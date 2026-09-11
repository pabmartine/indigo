package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.BookMetadataScope;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillBooksMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class StartFillBooksMetadataUseCaseImpl implements StartFillBooksMetadataUseCase {

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected CommandBus commandBus;

	@Override
	public void start(final BookMetadataScope scope, final MetadataMergePolicy mergePolicy,
			final DynamicMetadataPolicy dynamicPolicy, final long requestedRunId) {

		log.info("Finding metadata for books with scope {}", scope);

		final boolean managedRun = requestedRunId > 0;
		final long runId = managedRun ? requestedRunId : metadataSingleton.getRunId();
		if (managedRun && !metadataSingleton.isActive(runId)) {
			return;
		}

		final List<BookMongoEntity> books = scope == BookMetadataScope.INCOMPLETE
				? bookRepository.findBooksWithIncompleteMetadata()
				: bookRepository.findAllBookIds();
		final long numBooks = books.size();

		if (managedRun && !metadataSingleton.initializeRun(runId, "obtaining_metadata_books", numBooks)) {
			return;
		}
		if (!managedRun) {
			metadataSingleton.setMessage("obtaining_metadata_books");
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

		long lastExecution = 0L;
		try {
			for (BookMongoEntity book : books) {
				if (!(managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
					break;
				}
				try {
					final MetadataItemResult result = commandBus.executeAndWait(FindBookMetadataCommand.builder()
							.bookId(book.getId())
							.mergePolicy(mergePolicy)
							.dynamicPolicy(dynamicPolicy)
							.lastExecution(lastExecution)
							.build());
					metadataSingleton.record(runId, result == null ? MetadataItemResult.ERROR : result);
				}
				catch (RuntimeException exception) {
					log.error("Book metadata failed for {}", book.getId(), exception);
					metadataSingleton.record(runId, MetadataItemResult.ERROR);
				}
				lastExecution = System.currentTimeMillis();
				log.debug("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);
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
