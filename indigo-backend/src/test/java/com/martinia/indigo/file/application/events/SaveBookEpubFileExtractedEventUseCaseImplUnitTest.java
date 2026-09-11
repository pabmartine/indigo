package com.martinia.indigo.file.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.FileRepository;
import com.martinia.indigo.file.domain.model.File;
import com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaveBookEpubFileExtractedEventUseCaseImplUnitTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private EventBus eventBus;

    @Mock
    private UploadEpubFilesSingleton uploadEpubFilesSingleton;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private SaveBookEpubFileExtractedEventUseCaseImpl useCase;

    private BookOpf bookOpf;
    private Path testPath;
    private BookMongoEntity existingBook;

    private void prepareExistingFiles(Path root) throws Exception {
        Path library = java.nio.file.Files.createDirectory(root.resolve("library"));
        Path uploads = java.nio.file.Files.createDirectory(root.resolve("uploads"));
        Path directory = java.nio.file.Files.createDirectories(library.resolve("Test Author/Test Book (en)"));
        java.nio.file.Files.writeString(directory.resolve("old-name.epub"), "old");
        testPath = java.nio.file.Files.writeString(uploads.resolve("renamed.epub"), "new");
        existingBook.setPath(directory.toString());
        existingBook.setImage("enriched-image");
        existingBook.setIsbn13(List.of("9780306406157"));
        ReflectionTestUtils.setField(useCase, "endpointBook", library.toString());
        ReflectionTestUtils.setField(useCase, "uploadsPath", uploads.toString());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(floats = {0, 1, 2, -1})
    void equalLowerOrUnknownVersionIsDiscarded(float version, @org.junit.jupiter.api.io.TempDir Path root) throws Exception {
        prepareExistingFiles(root);
        existingBook.setVersion(2);
        bookOpf.setVersion(version);
        when(bookRepository.findByAnyIsbn(any())).thenReturn(List.of(existingBook));
        useCase.save(bookOpf, testPath);
        verify(bookRepository, never()).findByPath(anyString());
        verify(bookRepository, never()).save(any());
        assertEquals(false, java.nio.file.Files.exists(testPath));
        assertEquals("old", java.nio.file.Files.readString(Path.of(existingBook.getPath()).resolve("old-name.epub")));
        assertEquals(2, existingBook.getVersion());
    }

    @Test
    void failedDatabaseUpdateRestoresFile(@org.junit.jupiter.api.io.TempDir Path root) throws Exception {
        prepareExistingFiles(root);
        bookOpf.setVersion(3);
        when(bookRepository.findByAnyIsbn(any())).thenReturn(List.of(existingBook));
        when(bookRepository.save(any())).thenThrow(new IllegalStateException("database unavailable"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> useCase.save(bookOpf, testPath));
        assertEquals("old", java.nio.file.Files.readString(Path.of(existingBook.getPath()).resolve("old-name.epub")));
        assertEquals("new", java.nio.file.Files.readString(testPath));
        assertEquals(0, existingBook.getVersion());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void transactionCompletionControlsCleanup(boolean commit, @org.junit.jupiter.api.io.TempDir Path root) throws Exception {
        prepareExistingFiles(root);
        bookOpf.setTitle("  TEST   BOOK ");
        bookOpf.setVersion(3);
        when(bookRepository.findByAnyIsbn(any())).thenReturn(List.of(existingBook));
        when(bookRepository.save(any())).thenReturn(existingBook);
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        org.springframework.transaction.support.TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            useCase.save(bookOpf, testPath);
            assertEquals(true, java.nio.file.Files.exists(testPath));
            for (var synchronization : org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCompletion(commit ? 0 : 1);
            }
            assertEquals(!commit, java.nio.file.Files.exists(testPath));
            assertEquals(commit ? "new" : "old", java.nio.file.Files.readString(Path.of(existingBook.getPath()).resolve("old-name.epub")));
            assertEquals("enriched-image", existingBook.getImage());
            verifyNoInteractions(eventBus);
            try (var files = java.nio.file.Files.list(Path.of(existingBook.getPath()))) {
                assertEquals(1, files.count());
            }
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void differentIsbnMustNotOverwriteSamePath(@org.junit.jupiter.api.io.TempDir Path root) throws Exception {
        prepareExistingFiles(root);
        existingBook.setIsbn13(List.of("9788445011405"));
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.of(existingBook));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> useCase.save(bookOpf, testPath));
        verify(bookRepository, never()).save(any());
        assertEquals(true, java.nio.file.Files.exists(testPath));
    }

    @Test
    void conflictMustNotModifyDatabase(@org.junit.jupiter.api.io.TempDir Path directory) throws Exception {
        ReflectionTestUtils.setField(useCase, "endpointBook", directory.resolve("library").toString());
        Path source = java.nio.file.Files.writeString(directory.resolve("book.epub"), "new version");
        Path destination = java.nio.file.Files.createDirectories(directory.resolve("library/Test Author/Test Book (en)"));
        java.nio.file.Files.writeString(destination.resolve("book.epub"), "old version");
        useCase.save(bookOpf, source);
        verify(bookRepository, never()).save(any());
        verifyNoInteractions(fileRepository, eventBus);
        verify(uploadEpubFilesSingleton).addMoveError();
        assertEquals("new version", java.nio.file.Files.readString(source));
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "endpointBook", "/test/library");

        bookOpf = BookOpf.builder()
                .title("Test Book")
                .authors(List.of("Test Author"))
                .language("en")
                .comment("Test comment")
                .seriesName("Test Series")
                .seriesIndex(1)
                .pubDate(new Date())
                .lastModified(new Date())
                .pages(200)
                .bookImage("test-image.jpg")
                .authorImage("author-image.jpg")
                .version(1)
                .tags(List.of("fiction", "fantasy"))
                .isbn10(List.of("0306406152"))
                .isbn13(List.of("9780306406157"))
                .identifiers(Map.of("ISBN_10", List.of("0306406152"), "ISBN_13", List.of("9780306406157")))
                .build();

        testPath = Paths.get("/test/source/path");

        existingBook = BookMongoEntity.builder()
                .id(UUID.randomUUID().toString())
                .title("Test Book")
                .version(0)
                .build();
    }

    @Test
    void save_WithNewBook_ShouldCreateAndSaveNewBook() {
        // Given
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookMongoEntity.class))).thenAnswer(invocation -> {
            BookMongoEntity book = invocation.getArgument(0);
            book.setId(UUID.randomUUID().toString());
            return book;
        });
        doNothing().when(uploadEpubFilesSingleton).addNewBook();
        doNothing().when(eventBus).publish(any(EpubFileAddedEvent.class));

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).findByPath(anyString());
        ArgumentCaptor<BookMongoEntity> bookCaptor = ArgumentCaptor.forClass(BookMongoEntity.class);
        verify(bookRepository, times(1)).save(bookCaptor.capture());
        assertEquals(List.of("0306406152"), bookCaptor.getValue().getIsbn10());
        assertEquals(List.of("9780306406157"), bookCaptor.getValue().getIsbn13());
        verify(uploadEpubFilesSingleton, times(1)).addNewBook();
        verify(eventBus, times(1)).publish(any(EpubFileAddedEvent.class));
    }

    @Test
    void save_ShouldTrackSourceFileSoMoveCanDeleteTransientRecord() {
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookMongoEntity.class))).thenAnswer(invocation -> {
            BookMongoEntity book = invocation.getArgument(0);
            book.setId(UUID.randomUUID().toString());
            return book;
        });

        useCase.save(bookOpf, testPath);

        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        verify(fileRepository).save(fileCaptor.capture());
        assertEquals(testPath, fileCaptor.getValue().getPath());
    }

    @Test
    void save_WithExistingBookAndHigherVersion_ShouldUpdateExistingBook(@org.junit.jupiter.api.io.TempDir Path root) throws Exception {
        prepareExistingFiles(root);
        // Given
        bookOpf.setVersion(2);
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(BookMongoEntity.class))).thenReturn(existingBook);
        doNothing().when(uploadEpubFilesSingleton).addUpdatedBook();

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).findByPath(anyString());
        verify(bookRepository, times(1)).save(any(BookMongoEntity.class));
        verify(uploadEpubFilesSingleton, times(1)).addUpdatedBook();
        verifyNoInteractions(eventBus);
        assertEquals("new", java.nio.file.Files.readString(Path.of(existingBook.getPath()).resolve("old-name.epub")));
        assertEquals(false, java.nio.file.Files.exists(testPath));
    }

    @Test
    void save_WithExistingBookAndLowerVersion_ShouldDiscardWithoutChangingMetadata(@org.junit.jupiter.api.io.TempDir Path root) throws Exception {
        prepareExistingFiles(root);
        // Given
        existingBook.setVersion(2);
        bookOpf.setVersion(1);
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.of(existingBook));

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).findByPath(anyString());
        verify(bookRepository, never()).save(any(BookMongoEntity.class));
        assertEquals(2, existingBook.getVersion());
        assertEquals("enriched-image", existingBook.getImage());
        verify(uploadEpubFilesSingleton, never()).addUpdatedBook();
        verifyNoInteractions(eventBus);
        assertEquals(false, java.nio.file.Files.exists(testPath));
    }

    @Test
    void save_WithMultipleAuthors_ShouldUseDefaultAuthorPath() {
        // Given
        bookOpf.setAuthors(List.of("Author 1", "Author 2"));
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookMongoEntity.class))).thenAnswer(invocation -> {
            BookMongoEntity book = invocation.getArgument(0);
            book.setId(UUID.randomUUID().toString());
            return book;
        });
        doNothing().when(uploadEpubFilesSingleton).addNewBook();
        doNothing().when(eventBus).publish(any(EpubFileAddedEvent.class));

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).findByPath(contains("AA. VV."));
        verify(bookRepository, times(1)).save(any(BookMongoEntity.class));
        verify(uploadEpubFilesSingleton, times(1)).addNewBook();
        verify(eventBus, times(1)).publish(any(EpubFileAddedEvent.class));
    }

    @Test
    void save_WithAuthorContainingAmpersand_ShouldUseDefaultAuthorPath() {
        // Given
        bookOpf.setAuthors(List.of("Author 1 & Author 2"));
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookMongoEntity.class))).thenAnswer(invocation -> {
            BookMongoEntity book = invocation.getArgument(0);
            book.setId(UUID.randomUUID().toString());
            return book;
        });
        doNothing().when(uploadEpubFilesSingleton).addNewBook();
        doNothing().when(eventBus).publish(any(EpubFileAddedEvent.class));

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).findByPath(contains("AA. VV."));
        verify(bookRepository, times(1)).save(any(BookMongoEntity.class));
        verify(uploadEpubFilesSingleton, times(1)).addNewBook();
        verify(eventBus, times(1)).publish(any(EpubFileAddedEvent.class));
    }

    @Test
    void save_WithNoSeriesName_ShouldCreateBookWithoutSeries() {
        // Given
        bookOpf.setSeriesName(null);
        bookOpf.setSeriesIndex(0);
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookMongoEntity.class))).thenAnswer(invocation -> {
            BookMongoEntity book = invocation.getArgument(0);
            book.setId(UUID.randomUUID().toString());
            return book;
        });
        doNothing().when(uploadEpubFilesSingleton).addNewBook();
        doNothing().when(eventBus).publish(any(EpubFileAddedEvent.class));

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).save(any(BookMongoEntity.class));
        verify(uploadEpubFilesSingleton, times(1)).addNewBook();
        verify(eventBus, times(1)).publish(any(EpubFileAddedEvent.class));
    }

    @Test
    void should_not_save_book_epub_file_extracted_event_when_book_is_null() {
        // Given
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookMongoEntity.class))).thenReturn(null); // Simulate book not being saved

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).findByPath(anyString());
        verify(bookRepository, times(1)).save(any(BookMongoEntity.class));
        verify(uploadEpubFilesSingleton, never()).addNewBook(); // Should not add new book
        verify(eventBus, never()).publish(any(EpubFileAddedEvent.class)); // Should not publish event
    }
}
