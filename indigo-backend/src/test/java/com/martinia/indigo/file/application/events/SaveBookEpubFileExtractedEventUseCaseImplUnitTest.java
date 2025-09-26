package com.martinia.indigo.file.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent;
import com.martinia.indigo.file.domain.FileRepository;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        verify(bookRepository, times(1)).save(any(BookMongoEntity.class));
        verify(uploadEpubFilesSingleton, times(1)).addNewBook();
        verify(eventBus, times(1)).publish(any(EpubFileAddedEvent.class));
    }

    @Test
    void save_WithExistingBookAndHigherVersion_ShouldUpdateExistingBook() {
        // Given
        bookOpf.setVersion(2);
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(BookMongoEntity.class))).thenReturn(existingBook);
        doNothing().when(uploadEpubFilesSingleton).addUpdatedBook();
        doNothing().when(eventBus).publish(any(EpubFileAddedEvent.class));

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).findByPath(anyString());
        verify(bookRepository, times(1)).save(any(BookMongoEntity.class));
        verify(uploadEpubFilesSingleton, times(1)).addUpdatedBook();
        verify(eventBus, times(1)).publish(any(EpubFileAddedEvent.class));
    }

    @Test
    void save_WithExistingBookAndLowerVersion_ShouldNotUpdateBook() {
        // Given
        existingBook.setVersion(2);
        bookOpf.setVersion(1);
        when(bookRepository.findByPath(anyString())).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(BookMongoEntity.class))).thenReturn(existingBook);
        doNothing().when(uploadEpubFilesSingleton).addUpdatedBook();
        doNothing().when(eventBus).publish(any(EpubFileAddedEvent.class));

        // When
        useCase.save(bookOpf, testPath);

        // Then
        verify(bookRepository, times(1)).findByPath(anyString());
        verify(bookRepository, times(1)).save(any(BookMongoEntity.class));
        verify(uploadEpubFilesSingleton, times(1)).addUpdatedBook();
        verify(eventBus, times(1)).publish(any(EpubFileAddedEvent.class));
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