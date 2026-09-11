package com.martinia.indigo.file.application.events;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.events.AuthorAddedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveAuthorEpubFileEventUseCaseImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private EventBus eventBus;

    @Mock
    private UploadEpubFilesSingleton uploadEpubFilesSingleton;
    @Mock
    private com.martinia.indigo.metadata.application.MetadataActivityService activity;

    @InjectMocks
    private SaveAuthorEpubFileEventUseCaseImpl saveAuthorEpubFileEventUseCase;

    private BookMongoEntity mockBook;
    private AuthorMongoEntity mockAuthor;

    @BeforeEach
    void setUp() {
        Map<String, Integer> languages = new HashMap<>();
        languages.put("en", 1);

        mockBook = BookMongoEntity.builder()
                .id("book1")
                .title("Test Book")
                .authors(List.of("Test Author"))
                .languages(List.of("en"))
                .build();

        mockAuthor = AuthorMongoEntity.builder()
                .id("author1")
                .name("Test Author")
                .image("existing-image")
                .numBooks(NumBooksMongo.builder()
                        .total(5)
                        .languages(languages)
                        .build())
                .build();
    }

    @Test
    void missingImagePreservesExistingImage() {
        when(bookRepository.findById("book1")).thenReturn(Optional.of(mockBook));
        when(authorRepository.findByName("Test Author")).thenReturn(Optional.of(mockAuthor));
        saveAuthorEpubFileEventUseCase.save("book1", null);
        assertThat(mockAuthor.getImage()).isEqualTo("existing-image");
        verify(authorRepository).save(mockAuthor);
    }

    @Test
    void protectedImageIsNotReplacedWhenAnotherBookIsImported() {
        when(bookRepository.findById("book1")).thenReturn(Optional.of(mockBook));
        when(authorRepository.findByName("Test Author")).thenReturn(Optional.of(mockAuthor));
        when(activity.isLocked("AUTHORS", "author1")).thenReturn(true);
        saveAuthorEpubFileEventUseCase.save("book1", "incoming-image");
        assertThat(mockAuthor.getImage()).isEqualTo("existing-image");
        assertThat(mockAuthor.getNumBooks().getTotal()).isEqualTo(6);
    }

    @Test
    void save_WhenBookExistsAndAuthorExists_ShouldUpdateAuthor() {
        // Given
        String bookId = "book1";
        String authorImage = "new-author-image";

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        when(authorRepository.findByName("Test Author")).thenReturn(Optional.of(mockAuthor));

        // When
        saveAuthorEpubFileEventUseCase.save(bookId, authorImage);

        // Then
        ArgumentCaptor<AuthorMongoEntity> authorCaptor = ArgumentCaptor.forClass(AuthorMongoEntity.class);
        verify(authorRepository).save(authorCaptor.capture());

        AuthorMongoEntity savedAuthor = authorCaptor.getValue();
        assertThat(savedAuthor.getImage()).isEqualTo(authorImage);
        assertThat(savedAuthor.getMetadataSources().get("image")).isEqualTo("EPUB");
        assertThat(savedAuthor.getNumBooks().getTotal()).isEqualTo(6); // Incremented from 5

        verify(eventBus).publish(any(AuthorAddedEvent.class));
        verify(uploadEpubFilesSingleton, never()).addAuthor();
    }

    @Test
    void save_WhenBookExistsAndAuthorDoesNotExist_ShouldCreateNewAuthor() {
        // Given
        String bookId = "book1";
        String authorImage = "new-author-image";

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        when(authorRepository.findByName("Test Author")).thenReturn(Optional.empty());

        AuthorMongoEntity newAuthor = AuthorMongoEntity.builder()
                .id("new-author-id")
                .name("Test Author")
                .image(authorImage)
                .numBooks(NumBooksMongo.builder()
                        .total(1)
                        .languages(Map.of("en", 1))
                        .build())
                .build();

        when(authorRepository.save(any(AuthorMongoEntity.class))).thenReturn(newAuthor);

        // When
        saveAuthorEpubFileEventUseCase.save(bookId, authorImage);

        // Then
        ArgumentCaptor<AuthorMongoEntity> authorCaptor = ArgumentCaptor.forClass(AuthorMongoEntity.class);
        verify(authorRepository).save(authorCaptor.capture());

        AuthorMongoEntity savedAuthor = authorCaptor.getValue();
        assertThat(savedAuthor.getName()).isEqualTo("Test Author");
        assertThat(savedAuthor.getImage()).isEqualTo(authorImage);
        assertThat(savedAuthor.getNumBooks().getTotal()).isEqualTo(1);

        verify(uploadEpubFilesSingleton).addAuthor();
        verify(eventBus).publish(any(AuthorAddedEvent.class));
    }

    @Test
    void save_WhenBookDoesNotExist_ShouldNotProcessAnything() {
        // Given
        String bookId = "non-existent-book";
        String authorImage = "author-image";

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> saveAuthorEpubFileEventUseCase.save(bookId, authorImage));

        // Then
        verify(authorRepository, never()).findByName(anyString());
        verify(authorRepository, never()).save(any());
        verify(eventBus, never()).publish(any());
        verify(uploadEpubFilesSingleton, never()).addAuthor();
    }

    @Test
    void save_WhenAuthorIsVVAA_ShouldNormalizeToAAVV() {
        // Given
        String bookId = "book1";
        String authorImage = "author-image";

        BookMongoEntity bookWithSpecialAuthor = BookMongoEntity.builder()
                .id("book1")
                .title("Test Book")
                .authors(List.of("VV., AA."))
                .languages(List.of("en"))
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookWithSpecialAuthor));
        when(authorRepository.findByName("AA. VV.")).thenReturn(Optional.empty());

        AuthorMongoEntity newAuthor = AuthorMongoEntity.builder()
                .id("new-author-id")
                .name("AA. VV.")
                .image(authorImage)
                .numBooks(NumBooksMongo.builder()
                        .total(1)
                        .languages(Map.of("en", 1))
                        .build())
                .build();

        when(authorRepository.save(any(AuthorMongoEntity.class))).thenReturn(newAuthor);

        // When
        saveAuthorEpubFileEventUseCase.save(bookId, authorImage);

        // Then
        ArgumentCaptor<AuthorMongoEntity> authorCaptor = ArgumentCaptor.forClass(AuthorMongoEntity.class);
        verify(authorRepository).save(authorCaptor.capture());

        AuthorMongoEntity savedAuthor = authorCaptor.getValue();
        assertThat(savedAuthor.getName()).isEqualTo("AA. VV.");

        verify(authorRepository).findByName("AA. VV.");
    }

    @Test
    void save_WhenBookHasMultipleAuthors_ShouldProcessAllAuthors() {
        // Given
        String bookId = "book1";
        String authorImage = "author-image";

        BookMongoEntity bookWithMultipleAuthors = BookMongoEntity.builder()
                .id("book1")
                .title("Test Book")
                .authors(List.of("Author One", "Author Two"))
                .languages(List.of("en"))
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookWithMultipleAuthors));
        when(authorRepository.findByName("Author One")).thenReturn(Optional.empty());
        when(authorRepository.findByName("Author Two")).thenReturn(Optional.empty());

        AuthorMongoEntity author1 = AuthorMongoEntity.builder()
                .id("author1-id")
                .name("Author One")
                .build();

        AuthorMongoEntity author2 = AuthorMongoEntity.builder()
                .id("author2-id")
                .name("Author Two")
                .build();

        when(authorRepository.save(any(AuthorMongoEntity.class)))
                .thenReturn(author1)
                .thenReturn(author2);

        // When
        saveAuthorEpubFileEventUseCase.save(bookId, authorImage);

        // Then
        verify(authorRepository, times(2)).save(any(AuthorMongoEntity.class)); // Should save both authors
        verify(eventBus, times(2)).publish(any(AuthorAddedEvent.class)); // Should publish 2 events
        verify(uploadEpubFilesSingleton, times(2)).addAuthor(); // Should add 2 authors
    }

    @Test
    void save_WhenAuthorLanguageDoesNotExist_ShouldAddLanguageWithCountOne() {
        // Given
        String bookId = "book1";
        String authorImage = "author-image";

        AuthorMongoEntity authorWithEmptyLanguages = AuthorMongoEntity.builder()
                .id("author1")
                .name("Test Author")
                .image("existing-image")
                .numBooks(NumBooksMongo.builder()
                        .total(1)
                        .languages(new HashMap<>())
                        .build())
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        when(authorRepository.findByName("Test Author")).thenReturn(Optional.of(authorWithEmptyLanguages));

        // When
        saveAuthorEpubFileEventUseCase.save(bookId, authorImage);

        // Then
        ArgumentCaptor<AuthorMongoEntity> authorCaptor = ArgumentCaptor.forClass(AuthorMongoEntity.class);
        verify(authorRepository).save(authorCaptor.capture());

        AuthorMongoEntity savedAuthor = authorCaptor.getValue();
        assertThat(savedAuthor.getNumBooks().getLanguages()).isNotNull();
        assertThat(savedAuthor.getNumBooks().getLanguages()).containsKey("en");
    }
}
