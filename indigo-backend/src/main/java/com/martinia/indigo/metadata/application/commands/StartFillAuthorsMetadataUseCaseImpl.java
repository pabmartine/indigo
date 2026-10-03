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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

@Slf4j
@Service
public class StartFillAuthorsMetadataUseCaseImpl implements StartFillAuthorsMetadataUseCase {

	private static final int BATCH_SIZE = 100;
	@org.springframework.beans.factory.annotation.Value("${metadata.authors.progress-log-interval-millis:30000}")
	private long progressLogIntervalMillis = 30_000L;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	@Lazy
	protected CommandBus commandBus;

	@Resource
	private com.martinia.indigo.metadata.application.MetadataExecutionService executions;

	@Override
	public void start(boolean override, String lang, long requestedRunId) {

		log.info("Finding metadata for all authors library");

		final boolean managedRun = requestedRunId > 0;
		final long runId = managedRun ? requestedRunId : metadataSingleton.getRunId();
		if (managedRun && !metadataSingleton.isActive(runId)) {
			return;
		}
		try (AuthorProgress progress = new AuthorProgress(runId, progressLogIntervalMillis)) {
			if (!synchronizeMissingAuthorsFromBooks(() -> !managedRun || metadataSingleton.isActive(runId), progress)) return;

			progress.stage("Consultando idiomas de la biblioteca");
			List<String> languages = bookRepository.getBookLanguages();
			progress.stage("Contando autores para obtener metadatos");
			String process = "AUTHORS:" + (override ? "ALL" : "INCOMPLETE");
			List<String> pending;
			synchronized (metadataSingleton) {
				if (managedRun && !metadataSingleton.isActive(runId)) return;
				pending = executions.pending(process, authorRepository.findMetadataIds(languages));
			}
			long numAuthors = pending.size();
			log.info("Author metadata run {} ready: authors={} languages={}", runId, numAuthors, languages);

			if (managedRun && !metadataSingleton.initializeRun(runId, "obtaining_metadata_authors", numAuthors)) {
				return;
			}
			if (!managedRun) {
				metadataSingleton.setMessage("obtaining_metadata_authors");
				metadataSingleton.setTotal(metadataSingleton.getTotal() + numAuthors);
			}
			if (numAuthors == 0) {
				return;
			}

			long lastExecution = 0;

			int page = 0;
			int size = BATCH_SIZE;
			while (page * size < numAuthors) {

				if (!(managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
					break;
				}

				progress.stage("Leyendo lote de autores para obtener metadatos");
				List<AuthorMongoEntity> authors = authorRepository.findAllById(
						pending.subList(page * size, Math.min((page + 1) * size, pending.size())));

				if (!CollectionUtils.isEmpty(authors)) {
					for (AuthorMongoEntity author : authors) {

						if (!(managedRun ? metadataSingleton.isActive(runId) : metadataSingleton.isRunning())) {
							break;
						}

						log.info("Processing author {}", author.getName());
						progress.stage("Obteniendo descripción e imagen del autor " + author.getName());

						MetadataItemResult result;
						try {
							result = commandBus.executeAndWait(FindAuthorMetadataCommand.builder()
									.authorId(author.getId()).lang(lang).override(override).lastExecution(lastExecution).build());
						}
						catch (java.util.concurrent.CancellationException exception) {
							return;
						}
						catch (RuntimeException exception) {
							com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
							log.error("Author metadata failed for {}", author.getName(), exception);
							result = MetadataItemResult.ERROR;
						}
						if (result != null && result != MetadataItemResult.ERROR) {
							checkpoint(process, author.getId(), managedRun, runId);
						}
						metadataSingleton.record(runId, result == null ? MetadataItemResult.ERROR : result);

						lastExecution = System.currentTimeMillis();
						progress.processedAuthors++;

						log.debug("Processed {}/{} authors", metadataSingleton.getCurrent(), numAuthors);

					}
				}

				page++;

			}
		}
		catch (RuntimeException exception) {
			log.error("Author metadata run {} failed", runId, exception);
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

	private boolean synchronizeMissingAuthorsFromBooks(final BooleanSupplier active, final AuthorProgress progress) {
		final Map<String, NumBooksMongo> authors = new LinkedHashMap<>();
		String afterId = null;
		progress.stage("Leyendo autores e idiomas de los libros");
		while (active.getAsBoolean()) {
			List<BookMongoEntity> batch = bookRepository.findAuthorNamesBatch(afterId);
			if (batch.isEmpty()) break;
			if (!active.getAsBoolean()) return false;
			for (BookMongoEntity book : batch) {
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
			progress.booksRead += batch.size();
			progress.authorNames = authors.size();
			afterId = batch.getLast().getId();
		}

		final List<String> names = new ArrayList<>(authors.keySet());
		for (int offset = 0; offset < names.size(); offset += 1_000) {
			if (!active.getAsBoolean()) return false;
			final List<String> namesBatch = names.subList(offset, Math.min(offset + 1_000, names.size()));
			progress.stage("Comprobando autores existentes por lotes");
			final Set<String> existing = new HashSet<>();
			authorRepository.findNamesByNameIn(namesBatch).forEach(author -> existing.add(author.getName()));
			progress.checkedAuthors += namesBatch.size();
			if (!active.getAsBoolean()) return false;
			final List<AuthorMongoEntity> missing = namesBatch.stream().filter(name -> !existing.contains(name))
					.map(name -> AuthorMongoEntity.builder().name(name).numBooks(authors.get(name)).build()).toList();
			if (!missing.isEmpty()) {
				progress.stage("Guardando autores nuevos por lotes");
				authorRepository.saveAll(missing);
				progress.createdAuthors += missing.size();
			}
		}
		if (progress.createdAuthors > 0) authorRepository.clearCache();
		progress.report();
		return active.getAsBoolean();
	}

	private static final class AuthorProgress implements AutoCloseable {
		private final long runId;
		private final long started = System.nanoTime();
		private final ScheduledExecutorService scheduler;
		private volatile String stage = "Iniciando sincronización de autores";
		private volatile long stageStarted = started;
		private volatile long booksRead;
		private volatile long authorNames;
		private volatile long checkedAuthors;
		private volatile long createdAuthors;
		private volatile long processedAuthors;

		private AuthorProgress(long runId, long intervalMillis) {
			this.runId = runId;
			scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
				Thread thread = new Thread(runnable, "author-metadata-progress");
				thread.setDaemon(true);
				return thread;
			});
			report();
			long interval = Math.max(1L, intervalMillis);
			scheduler.scheduleAtFixedRate(this::report, interval, interval, TimeUnit.MILLISECONDS);
		}

		private void stage(String value) {
			if (!value.equals(stage)) {
				stage = value;
				stageStarted = System.nanoTime();
				report();
			}
		}

		private void report() {
			long now = System.nanoTime();
			log.info("Author metadata run {} progress: stage={} booksRead={} authorNames={} checkedAuthors={} createdAuthors={} processedAuthors={} elapsedMs={} stageElapsedMs={}",
					runId, stage, booksRead, authorNames, checkedAuthors, createdAuthors, processedAuthors,
					(now - started) / 1_000_000L, (now - stageStarted) / 1_000_000L);
		}

		@Override
		public void close() {
			scheduler.shutdownNow();
			report();
			log.info("Author metadata run {} ended", runId);
		}
	}
}
