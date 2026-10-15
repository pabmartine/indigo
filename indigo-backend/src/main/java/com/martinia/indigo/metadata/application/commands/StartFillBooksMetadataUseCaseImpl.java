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

    @org.springframework.beans.factory.annotation.Value("${metadata.books.progress-log-interval-millis:30000}")
    private long progressLogIntervalMillis = 30000;

	@Override
	public void start(final BookMetadataScope scope, final MetadataMergePolicy mergePolicy,
			final DynamicMetadataPolicy dynamicPolicy, final long requestedRunId) {

		log.info("Finding metadata for books with scope {}", scope);

		final boolean managedRun = requestedRunId > 0;
		final long runId = managedRun ? requestedRunId : metadataSingleton.getRunId();
		if (managedRun && !metadataSingleton.isActive(runId)) {
			return;
		}

        try (BookProgress progress = new BookProgress(runId, progressLogIntervalMillis)) {
			final List<BookMongoEntity> selected = scope == BookMetadataScope.INCOMPLETE
					? bookRepository.findBooksWithIncompleteMetadata()
					: bookRepository.findAllBookIds();
            progress.stage = "Comprobar libros pendientes";
			final String process = "BOOKS:" + scope.name();
			final List<String> pending;
			synchronized (metadataSingleton) {
				if (managedRun && !metadataSingleton.isActive(runId)) return;
				pending = executions.pending(process, selected.stream().map(BookMongoEntity::getId).toList());
			}
			final java.util.Set<String> pendingIds = new java.util.HashSet<>(pending);
			final List<BookMongoEntity> books = selected.stream().filter(book -> pendingIds.contains(book.getId())).toList();
			final long numBooks = books.size();
            progress.total = numBooks;

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
			for (BookMongoEntity book : books) {
				if (!(managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
					break;
				}
                progress.stage = "Consultar libro " + book.getId();
                long itemStarted = System.nanoTime();
				MetadataItemResult result;
				try {
					result = commandBus.executeAndWait(FindBookMetadataCommand.builder()
							.bookId(book.getId())
							.runId(managedRun ? runId : 0)
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
				if (result != null && result != MetadataItemResult.ERROR && result != MetadataItemResult.DEFERRED) {
					checkpoint(process, book.getId(), managedRun, runId);
				}
				metadataSingleton.record(runId, result == null ? MetadataItemResult.ERROR : result);
                progress.processed++;
                log.info("Book metadata run {} item={} result={} durationMs={}", runId, book.getId(), result,
                        (System.nanoTime() - itemStarted) / 1_000_000L);
				lastExecution = System.currentTimeMillis();
				log.debug("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);
			}
		}
        catch (RuntimeException exception) {
            com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
            log.error("Book metadata run {} failed", runId, exception);
            if (managedRun) metadataSingleton.fail(runId);
            throw exception;
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

    private static final class BookProgress implements AutoCloseable {
        private final long runId;
        private final long started = System.nanoTime();
        private final java.util.concurrent.ScheduledExecutorService scheduler;
        private volatile String stage = "Seleccionar libros";
        private volatile long total;
        private volatile long processed;

        private BookProgress(long runId, long intervalMillis) {
            this.runId = runId;
            scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "book-metadata-progress");
                thread.setDaemon(true);
                return thread;
            });
            long interval = Math.max(1, intervalMillis);
            scheduler.scheduleAtFixedRate(this::report, interval, interval, java.util.concurrent.TimeUnit.MILLISECONDS);
            report();
        }

        private void report() {
            log.info("Book metadata run {} progress: stage={} processed={} total={} elapsedMs={}",
                    runId, stage, processed, total, (System.nanoTime() - started) / 1_000_000L);
        }

        @Override public void close() {
            scheduler.shutdownNow();
            report();
            log.info("Book metadata run {} ended", runId);
        }
    }

}
