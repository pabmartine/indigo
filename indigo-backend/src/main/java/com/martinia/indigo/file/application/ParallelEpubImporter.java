package com.martinia.indigo.file.application;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveBookEpubFileExtractedEventUseCase;
import jakarta.annotation.Resource;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ParallelEpubImporter {
    @Resource private PreparedEpubReader reader;
    @Resource private SaveBookEpubFileExtractedEventUseCase saver;
    @Resource private PendingImportService pending;
    @Resource private UploadEpubFilesSingleton progress;
    @Value("${book.library.import-workers:2}") private int workers;
    private final Object persistenceLock = new Object();

    public void process(List<Path> paths) {
        java.util.Queue<String> categoryBooks = new java.util.concurrent.ConcurrentLinkedQueue<>();
        int size = Math.max(1, Math.min(8, workers));
        ExecutorService pool = Executors.newFixedThreadPool(size);
        CompletionService<Void> completions = new ExecutorCompletionService<>(pool);
        int submitted = 0, finished = 0;
        try {
            while (finished < paths.size()) {
                while (submitted < paths.size() && submitted - finished < size && progress.isRunning()) {
                    Path path = paths.get(submitted++);
                    completions.submit(() -> { processOne(path, categoryBooks); return null; });
                }
                if (finished == submitted) break;
                try { completions.take().get(); } catch (ExecutionException failure) {
                    org.slf4j.LoggerFactory.getLogger(getClass()).error("Import worker failed", failure.getCause());
                }
                finished++;
            }
        } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
        finally {
            pool.shutdown();
            boolean interrupted = Thread.interrupted();
            while (!pool.isTerminated()) {
                try { pool.awaitTermination(1, TimeUnit.SECONDS); }
                catch (InterruptedException error) { interrupted = true; }
            }
            if (interrupted) Thread.currentThread().interrupt();
            pending.completeBatchCategories(List.copyOf(categoryBooks));
        }
    }
    private void processOne(Path source, java.util.Queue<String> categoryBooks) {
        PreparedEpubReader.Prepared epub;
        try { epub = reader.read(source); progress.addExtract(); }
        catch (Exception error) {
            progress.addExtractError();
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Could not prepare EPUB {}", source, error);
            return;
        }
        try (epub) {
            // Includes transaction commit and file/author/category completion, not only the Java save method.
            synchronized (persistenceLock) {
                try (ImportExecution scope = new ImportExecution(epub::loadImages)) {
                    saver.save(epub.opf(), epub.path());
                    if (scope.event() != null) {
                        categoryBooks.add(scope.event().getBookId());
                        var result = pending.finishManaged(scope.event().getBookId(), false);
                        if (result.getList("pendingTasks", String.class).stream().anyMatch(task -> !"tagsDone".equals(task)))
                            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Import {} still has pending tasks: {}", source, result.get("pendingTasks"));
                    }
                }
            }
        } catch (Exception error) {
            progress.addMoveError();
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Could not complete EPUB {}; source retained where possible", source, error);
        }
    }
}
