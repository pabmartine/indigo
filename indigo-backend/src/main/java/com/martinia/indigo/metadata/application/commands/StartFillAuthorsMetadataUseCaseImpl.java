package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillAuthorsMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
@Transactional
public class StartFillAuthorsMetadataUseCaseImpl implements StartFillAuthorsMetadataUseCase {

	private static final int BATCH_SIZE = 100;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	protected CommandBus commandBus;

	@Override
	@Transactional
	public void start(boolean override, String lang) {

		log.info("Finding metadata for all authors library");

		metadataSingleton.setMessage("obtaining_metadata_authors");

		List<String> languages = bookRepository.getBookLanguages();
		Long numAuthors = authorRepository.count(languages);

		metadataSingleton.setTotal(metadataSingleton.getTotal() + numAuthors);

		long lastExecution = 0;

		int page = 0;
		int size = BATCH_SIZE;
		while (page * size < numAuthors) {

			if (!metadataSingleton.isRunning()) {
				break;
			}

			List<AuthorMongoEntity> authors = authorRepository.findAll(languages,
					PageRequest.of(page, size, Sort.by(Sort.Direction.fromString("asc"), "id")));

			if (!CollectionUtils.isEmpty(authors)) {
				for (AuthorMongoEntity author : authors) {

					if (!metadataSingleton.isRunning()) {
						break;
					}

					commandBus.executeAndWait(FindAuthorMetadataCommand.builder()
							.authorId(author.getId())
							.lang(lang)
							.override(override)
							.lastExecution(lastExecution)
							.build());

					lastExecution = System.currentTimeMillis();

					metadataSingleton.increase();

					log.debug("Obtained {}/{} authors metadata", metadataSingleton.getCurrent(), numAuthors);

				}
			}

			page++;

		}

	}

}
