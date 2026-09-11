package com.martinia.indigo.file.application;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import java.nio.file.*;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportRecoveryJournalTest {
    @ParameterizedTest @ValueSource(booleans = {true, false})
    void restartRecoversAccordingToDatabaseCommit(boolean committed, @TempDir Path root) throws Exception {
        Path library = Files.createDirectory(root.resolve("library"));
        Path uploads = Files.createDirectory(root.resolve("uploads"));
        Path source = Files.writeString(uploads.resolve("new.epub"), "new");
        Path target = Files.writeString(library.resolve("book.epub"), "new");
        Path backup = Files.writeString(library.resolve(".import-test.bak"), "old");
        var books = mock(BookRepository.class);
        when(books.findById("book")).thenReturn(Optional.of(BookMongoEntity.builder().version(committed ? 2 : 1).build()));
        var journal = new ImportRecoveryJournal();
        ReflectionTestUtils.setField(journal, "books", books);
        ReflectionTestUtils.setField(journal, "library", library.toString());
        ReflectionTestUtils.setField(journal, "uploads", uploads.toString());
        journal.prepare("book", 2, source, target, backup);
        journal.recover();
        assertEquals(committed ? "new" : "old", Files.readString(target));
        assertEquals(!committed, Files.exists(source));
        assertFalse(Files.exists(backup));
        assertFalse(Files.exists(ImportRecoveryJournal.marker(backup)));
        journal.recover();
        assertEquals(committed ? "new" : "old", Files.readString(target));
    }
}
