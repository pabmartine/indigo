package com.martinia.indigo.file.application;

import com.martinia.indigo.file.domain.ports.usecases.CountEpubFilesUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class CountEpubFilesUseCaseImpl implements CountEpubFilesUseCase {
    private static final long COUNT_TTL = Duration.ofSeconds(30).toNanos();
    private static final long SELECTION_TTL = Duration.ofMinutes(10).toNanos();
    private final EpubFileScanner scanner = new EpubFileScanner();
    private List<Path> detected;
    private long completedAt;

    @Value("${book.library.uploads}")
    private String uploadsPath;

    // Coalesce concurrent clicks; cache age starts after the scan finishes, not before it starts.
    @Override
    public synchronized Long count() {
        try {
            if (detected == null || System.nanoTime() - completedAt >= COUNT_TTL) refresh();
            return (long) detected.size();
        } catch (IOException error) {
            invalidate();
            log.error("Could not detect EPUBs in {}", uploadsPath, error);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer la carpeta de libros", error);
        }
    }

    /** Consume the detected paths. Added files are picked up by the next detection, missing files fail individually. */
    public synchronized List<Path> takePaths(long limit) throws IOException {
        if (detected == null || System.nanoTime() - completedAt >= SELECTION_TTL) refresh();
        List<Path> selected = List.copyOf(detected.subList(0, (int) Math.min(limit, detected.size())));
        invalidate();
        return selected;
    }

    public synchronized void invalidate() { detected = null; }

    private void refresh() throws IOException {
        long started = System.nanoTime();
        detected = List.copyOf(scanner.scan(Path.of(uploadsPath)));
        completedAt = System.nanoTime();
        log.info("Detected {} EPUB files in {} ms: {}", detected.size(), (completedAt - started) / 1_000_000L, uploadsPath);
    }
}
