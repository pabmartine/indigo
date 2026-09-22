package com.martinia.indigo.file.application;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveBookEpubFileExtractedEventUseCase;
import jakarta.annotation.Resource;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ParallelEpubImporter {
    @Resource private PreparedEpubReader reader;
    @Resource private SaveBookEpubFileExtractedEventUseCase saver;
    @Resource private PendingImportService pending;
    @Resource private UploadEpubFilesSingleton progress;
    @Value("${book.library.import-workers:2}") private int workers;
    private final Object persistenceLock = new Object();

    private record Stage(Thread thread, String name, long since) { }

    public void process(List<Path> paths) {
        var active = new ConcurrentHashMap<Path, Stage>();
        java.util.Queue<String> categoryBooks = new java.util.concurrent.ConcurrentLinkedQueue<>();
        int size = Math.max(1, Math.min(8, workers));
        log.info("Starting EPUB import with {} parallel worker(s) for {} files", size, paths.size());
        ExecutorService pool = Executors.newFixedThreadPool(size, new ThreadFactory() {
            private final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "epub-import-worker-" + count.getAndIncrement());
            }
        });
        CompletionService<Void> completions = new ExecutorCompletionService<>(pool);
        int submitted = 0, finished = 0;
        try {
            while (finished < paths.size()) {
                while (submitted < paths.size() && submitted - finished < size && progress.isRunning()) {
                    Path path = paths.get(submitted++);
                    completions.submit(() -> { processOne(path, categoryBooks, active); return null; });
                }
                if (finished == submitted) break;
                Future<Void> completed = completions.poll(30, TimeUnit.SECONDS);
                if (completed == null) {
                    active.forEach((path, stage) -> {
                        var stack = stage.thread().getStackTrace();
                        log.warn("EPUB still processing: file={} phase={} phaseSeconds={} worker={} state={} stack={}",
                                path, stage.name(), TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - stage.since()),
                                stage.thread().getName(), stage.thread().getState(),
                                java.util.Arrays.toString(java.util.Arrays.copyOf(stack, Math.min(stack.length, 8))));
                    });
                    continue;
                }
                try { completed.get(); } catch (ExecutionException failure) {
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
    private void processOne(Path source, java.util.Queue<String> categoryBooks, ConcurrentMap<Path, Stage> active) {
        try {
            processTracked(source, categoryBooks, active);
        } finally { active.remove(source); }
    }

    private void stage(Path source, String name, ConcurrentMap<Path, Stage> active) {
        active.put(source, new Stage(Thread.currentThread(), name, System.nanoTime()));
        log.debug("EPUB phase: file={} phase={}", source, name);
    }

    private void processTracked(Path source, java.util.Queue<String> categoryBooks, ConcurrentMap<Path, Stage> active) {
        stage(source, "read-opf", active);
        log.info("Processing EPUB {} on worker thread {}", source.getFileName(), Thread.currentThread().getName());
        PreparedEpubReader.Prepared epub;
        try { epub = reader.read(source); progress.addExtract(); }
        catch (Exception error) {
            progress.addExtractError();
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Could not prepare EPUB {}", source, error);
            return;
        }
        try (epub) {
            // Includes transaction commit and file/author/category completion, not only the Java save method.
            stage(source, "waiting-persistence", active);
            synchronized (persistenceLock) {
                try (ImportExecution scope = new ImportExecution(epub::loadImages)) {
                    stage(source, "save-book", active);
                    saver.save(epub.opf(), epub.path());
                    if (scope.event() != null) {
                        categoryBooks.add(scope.event().getBookId());
                        stage(source, "complete-file-and-authors", active);
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
