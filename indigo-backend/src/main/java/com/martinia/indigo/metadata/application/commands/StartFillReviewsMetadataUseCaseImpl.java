package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.commands.FindReviewMetadataCommand;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillReviewsMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class StartFillReviewsMetadataUseCaseImpl implements StartFillReviewsMetadataUseCase {

	private static final int BATCH_SIZE = 100;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	protected CommandBus commandBus;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.reviews.ReviewQueueService reviewQueue;

	@Override
	public void start(boolean override, String lang, long requestedRunId) {
		if (reviewQueue != null) {
			reviewQueue.start(override, lang, false);
			if (requestedRunId > 0) metadataSingleton.complete(requestedRunId);
			return;
		}

		log.info("Finding reviews for all book library");

		final boolean managedRun = requestedRunId > 0;
		final long runId = managedRun ? requestedRunId : metadataSingleton.getRunId();
		if (managedRun && !metadataSingleton.isActive(runId)) {
			return;
		}

		Long numBooks = bookRepository.count();

		if (managedRun && !metadataSingleton.initializeRun(runId, "obtaining_metadata_reviews", numBooks)) {
			return;
		}
		if (!managedRun) {
			metadataSingleton.setMessage("obtaining_metadata_reviews");
			metadataSingleton.setTotal(metadataSingleton.getTotal() + numBooks);
		}
		if (numBooks == 0) {
			if (managedRun) {
				metadataSingleton.complete(runId);
			}
			else {
				metadataSingleton.complete();
			}
			return;
		}

		int page = 0;
		int size = BATCH_SIZE;
		try {
			while (page * size < numBooks) {

			if (!(managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
				break;
			}

			List<BookMongoEntity> books = bookRepository.findAll(null, page, size, "id", "asc");

			if (!CollectionUtils.isEmpty(books)) {
				for (BookMongoEntity book : books) {

					if (!(managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
						break;
					}

					try {
						MetadataItemResult result = commandBus.executeAndWait(
								FindReviewMetadataCommand.builder().bookId(book.getId()).override(override).lang(lang).build());
						metadataSingleton.record(runId, result);
					}
					catch (RuntimeException exception) {
						log.error("Review metadata failed for {}", book.getTitle(), exception);
						metadataSingleton.record(runId, MetadataItemResult.ERROR);
					}

					log.debug("Obtained {}/{} books reviews", metadataSingleton.getCurrent(), numBooks);
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

}
