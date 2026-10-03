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

	@Resource
	private com.martinia.indigo.metadata.application.MetadataExecutionService executions;

	@Override
	public void start(final BookMetadataScope scope, final MetadataMergePolicy mergePolicy,
			final DynamicMetadataPolicy dynamicPolicy, final long requestedRunId) {

		log.info("Finding metadata for books with scope {}", scope);

		final boolean managedRun = requestedRunId > 0;
		final long runId = managedRun ? requestedRunId : metadataSingleton.getRunId();
		if (managedRun && !metadataSingleton.isActive(runId)) {
			return;
		}

		final List<BookMongoEntity> selected = scope == BookMetadataScope.INCOMPLETE
				? bookRepository.findBooksWithIncompleteMetadata()
				: bookRepository.findAllBookIds();
		final String process = "BOOKS:" + scope.name();
		final List<String> pending;
		synchronized (metadataSingleton) {
			if (managedRun && !metadataSingleton.isActive(runId)) return;
			pending = executions.pending(process, selected.stream().map(BookMongoEntity::getId).toList());
		}
		final java.util.Set<String> pendingIds = new java.util.HashSet<>(pending);
		final List<BookMongoEntity> books = selected.stream().filter(book -> pendingIds.contains(book.getId())).toList();
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
				MetadataItemResult result;
				try {
					result = commandBus.executeAndWait(FindBookMetadataCommand.builder()
							.bookId(book.getId())
							.mergePolicy(mergePolicy)
							.dynamicPolicy(dynamicPolicy)
							.lastExecution(lastExecution)
							.build());
				}
				catch (java.util.concurrent.CancellationException exception) {
					break;
				}
				catch (RuntimeException exception) {
					com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
					log.error("Book metadata failed for {}", book.getId(), exception);
					result = MetadataItemResult.ERROR;
				}
				checkpoint(process, book.getId(), managedRun, runId);
				metadataSingleton.record(runId, result == null ? MetadataItemResult.ERROR : result);
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

	private void checkpoint(String process, String id, boolean managed, long runId) {
		synchronized (metadataSingleton) {
			if (!Thread.currentThread().isInterrupted()
					&& (managed ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
				executions.inspected(process, id);
			}
		}
	}

}
