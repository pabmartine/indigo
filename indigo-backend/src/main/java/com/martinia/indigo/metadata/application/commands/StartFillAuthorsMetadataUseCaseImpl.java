package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillAuthorsMetadataUseCase;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StartFillAuthorsMetadataUseCaseImpl implements StartFillAuthorsMetadataUseCase {

	private static final int BATCH_SIZE = 100;

    @org.springframework.beans.factory.annotation.Autowired
    private com.martinia.indigo.common.util.DataUtils dataUtils;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	@Lazy
	protected CommandBus commandBus;

	@Override
	public void start(boolean override, String lang, long requestedRunId) {

		log.info("Finding metadata for all authors library");

		final boolean managedRun = requestedRunId > 0;
		final long runId = managedRun ? requestedRunId : metadataSingleton.getRunId();
		if (managedRun && !metadataSingleton.isActive(runId)) {
			return;
		}
		synchronizeMissingAuthorsFromBooks();

		List<String> languages = bookRepository.getBookLanguages();
		Long numAuthors = authorRepository.count(languages);

		if (managedRun && !metadataSingleton.initializeRun(runId, "obtaining_metadata_authors", numAuthors)) {
			return;
		}
		if (!managedRun) {
			metadataSingleton.setMessage("obtaining_metadata_authors");
			metadataSingleton.setTotal(metadataSingleton.getTotal() + numAuthors);
		}
		if (numAuthors == 0) {
			if (managedRun) {
				metadataSingleton.complete(runId);
			}
			else {
				metadataSingleton.complete();
			}
			return;
		}

		long lastExecution = 0;

		int page = 0;
		int size = BATCH_SIZE;
		try {
			while (page * size < numAuthors) {

			if (!(managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
				break;
			}

			List<AuthorMongoEntity> authors = authorRepository.findAll(languages,
					PageRequest.of(page, size, Sort.by(Sort.Direction.fromString("asc"), "id")));

			if (!CollectionUtils.isEmpty(authors)) {
				for (AuthorMongoEntity author : authors) {

					if (!(managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
						break;
					}

					if (dataUtils != null && !dataUtils.awaitWikipediaAvailable(
                            () -> managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
                        break;
                    }
                    log.info("Processing author {}", author.getName());

					try {
						MetadataItemResult result = commandBus.executeAndWait(FindAuthorMetadataCommand.builder()
								.authorId(author.getId()).lang(lang).override(override).lastExecution(lastExecution).build());
                        for (int retry = 1; result == MetadataItemResult.ERROR && dataUtils != null
                                && dataUtils.isWikipediaPaused() && retry <= 3; retry++) {
                            log.info("Retrying author {} after Wikipedia pause (retry {}/3)", author.getName(), retry);
                            if (!dataUtils.awaitWikipediaAvailable(
                                    () -> managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) break;
                            result = commandBus.executeAndWait(FindAuthorMetadataCommand.builder()
                                    .authorId(author.getId()).lang(lang).override(override).lastExecution(lastExecution).build());
                        }
                        metadataSingleton.record(runId, result);
					}
					catch (RuntimeException exception) {
						log.error("Author metadata failed for {}", author.getName(), exception);
						metadataSingleton.record(runId, MetadataItemResult.ERROR);
					}

					lastExecution = System.currentTimeMillis();

					log.debug("Processed {}/{} authors", metadataSingleton.getCurrent(), numAuthors);

				}
			}

				page++;

			}
		}
		finally {
			if (managedRun) {
				metadataSingleton.complete(runId);
			}
			else {
				metadataSingleton.complete();
			}
		}

	}

	private void synchronizeMissingAuthorsFromBooks() {
		final Map<String, NumBooksMongo> authors = new LinkedHashMap<>();
		final long numBooks = bookRepository.count();
		for (int page = 0; page * BATCH_SIZE < numBooks; page++) {
			for (BookMongoEntity book : bookRepository.findAll(null, page, BATCH_SIZE, "id", "asc")) {
				if (book.getAuthors() == null) {
					continue;
				}
				for (String rawAuthor : book.getAuthors()) {
					if (rawAuthor == null || rawAuthor.isBlank()) {
						continue;
					}
					final String author = rawAuthor.equalsIgnoreCase("VV., AA.") ? "AA. VV." : rawAuthor;
					final NumBooksMongo stats = authors.computeIfAbsent(author,
							ignored -> NumBooksMongo.builder().total(0).languages(new LinkedHashMap<>()).build());
					stats.setTotal(stats.getTotal() + 1);
					if (book.getLanguages() != null) {
						book.getLanguages().forEach(language -> stats.getLanguages().merge(language, 1, Integer::sum));
					}
				}
			}
		}

		authors.forEach((name, stats) -> authorRepository.findByName(name).orElseGet(() -> {
			log.info("Creating missing author {} from imported books", name);
			return authorRepository.save(AuthorMongoEntity.builder().name(name).numBooks(stats).build());
		}));
	}

}
