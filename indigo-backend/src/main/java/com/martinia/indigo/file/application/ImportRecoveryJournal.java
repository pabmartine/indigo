package com.martinia.indigo.file.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.nio.file.*;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/** A journal lives on the same persistent volume as the EPUB and its recovery copy. */
@Service
@Slf4j
public class ImportRecoveryJournal {
    @Resource private BookRepository books;
    @Value("${book.library.path}") private String library;
    @Value("${book.library.uploads}") private String uploads;
    private final ObjectMapper mapper = new ObjectMapper();
    public record Entry(String bookId, float version, String source, String target, String backup) { }

    public void prepare(String id, float version, Path source, Path target, Path backup) throws IOException {
        Path journal = marker(backup);
        Path temporary = Files.createTempFile(backup.getParent(), ".journal-", ".tmp");
        try {
            mapper.writeValue(temporary.toFile(), new Entry(id, version, source.toAbsolutePath().toString(),
                    target.toAbsolutePath().toString(), backup.toAbsolutePath().toString()));
            Files.move(temporary, journal, StandardCopyOption.ATOMIC_MOVE);
        } finally { Files.deleteIfExists(temporary); }
    }
    public static Path marker(Path backup) { return Path.of(backup + ".json"); }

    @PostConstruct
    public void recover() {
        Path root = Path.of(library).toAbsolutePath().normalize();
        if (root.getParent() == null || root.equals(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize())
                || root.equals(Path.of(System.getProperty("user.home")).toAbsolutePath().normalize())) {
            log.warn("Automatic recovery requires a dedicated library directory; broad root rejected");
            return;
        }
        if (!Files.isDirectory(root)) return;
        try (var files = Files.walk(root)) {
            files.filter(path -> path.getFileName().toString().matches("\\.import-.*\\.bak\\.json"))
                    .toList().forEach(this::recoverEntry);
        } catch (IOException | java.io.UncheckedIOException exception) { log.error("Import recovery scan failed", exception); }
    }

    private void recoverEntry(Path journal) {
        try {
            Entry entry = mapper.readValue(journal.toFile(), Entry.class);
            Path root = Path.of(library).toRealPath();
            Path backup = Path.of(entry.backup()).toAbsolutePath().normalize();
            Path target = Path.of(entry.target()).toAbsolutePath().normalize();
            Path source = Path.of(entry.source()).toAbsolutePath().normalize();
            if (!marker(backup).equals(journal.toAbsolutePath().normalize())
                    || !target.getParent().toRealPath().startsWith(root)
                    || !backup.getParent().equals(target.getParent())
                    || !source.getParent().toRealPath().startsWith(Path.of(uploads).toRealPath())
                    || source.startsWith(root) || Files.isSymbolicLink(target) || Files.isSymbolicLink(backup)
                    || Files.isSymbolicLink(source)) throw new IOException("Unsafe recovery paths");
            var book = books.findById(entry.bookId()).orElse(null);
            if (book != null && Float.compare(book.getVersion(), entry.version()) >= 0) {
                // Database commit completed before the crash. Keep the installed EPUB.
                if (!Files.isRegularFile(target)) throw new IOException("Committed EPUB is missing");
                Files.deleteIfExists(source);
            } else {
                Files.copy(backup, target, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.deleteIfExists(backup);
            Files.delete(journal);
            log.info("Recovered interrupted import for {}", entry.bookId());
        } catch (Exception exception) { log.error("Recovery requires attention; files retained at {}", journal, exception); }
    }
}
