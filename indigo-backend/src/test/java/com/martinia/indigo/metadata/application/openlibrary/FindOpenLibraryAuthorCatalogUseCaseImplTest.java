package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryAuthorRepository;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryIndexJobRepository;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryAuthorMongoEntity;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryIndexJobMongoEntity;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindOpenLibraryAuthorCatalogUseCaseImplTest {
    @Mock private OpenLibraryAuthorRepository authorRepository;
    @Mock private OpenLibraryIndexJobRepository jobRepository;
    @Mock private CachedSpanishTranslation spanishTranslation;
    @InjectMocks private FindOpenLibraryAuthorCatalogUseCaseImpl useCase;

    @BeforeEach void setUp() {
        ReflectionTestUtils.setField(useCase, "authorImageEndpoint", "https://covers.openlibrary.org/a/olid/$id-L.jpg?default=false");
    }

    private void activeIndex() {
        when(jobRepository.findById(OpenLibraryIndexManager.JOB_ID)).thenReturn(Optional.of(
                OpenLibraryIndexJobMongoEntity.builder().activeVersion("active").authorsVersion("active").stagingVersion("staging").build()));
    }

    @Test void translatesBiographyFromActiveDumpAndReturnsPhoto() {
        activeIndex();
        when(authorRepository.findTop2ByIndexVersionAndNames("active", "j r r tolkien")).thenReturn(List.of(
                OpenLibraryAuthorMongoEntity.builder().authorId("OL26320A").biography("British writer").hasPhoto(true).build()));
        when(spanishTranslation.translate("British writer")).thenReturn("Escritor británico");
        assertThat(useCase.findAuthor("J. R. R. Tolkien")).containsExactly("Escritor británico",
                "https://covers.openlibrary.org/a/olid/OL26320A-L.jpg?default=false", "OPEN_LIBRARY");
    }

    @Test void missingOrAmbiguousAuthorDoesNotTranslate() {
        activeIndex();
        when(authorRepository.findTop2ByIndexVersionAndNames("active", "author"))
                .thenReturn(List.of(), List.of(OpenLibraryAuthorMongoEntity.builder().build(), OpenLibraryAuthorMongoEntity.builder().build()));
        assertThat(useCase.findAuthor("Author")).isNull();
        assertThat(useCase.findAuthor("Author")).isNull();
        verifyNoInteractions(spanishTranslation);
    }

    @Test void translationFailureRemainsRetryable() {
        activeIndex();
        when(authorRepository.findTop2ByIndexVersionAndNames("active", "author")).thenReturn(List.of(
                OpenLibraryAuthorMongoEntity.builder().biography("Biography").build()));
        assertThatThrownBy(() -> useCase.findAuthor("Author")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("translation unavailable");
    }

    @Test void fallsBackWhenOldIndexDoesNotContainAuthors() {
        when(jobRepository.findById(OpenLibraryIndexManager.JOB_ID)).thenReturn(Optional.of(
                OpenLibraryIndexJobMongoEntity.builder().activeVersion("old").build()));
        assertThat(useCase.findAuthor("Author")).isNull();
        verifyNoInteractions(authorRepository, spanishTranslation);
    }

    @Test void supportsPhotoWithoutBiography() {
        activeIndex();
        when(authorRepository.findTop2ByIndexVersionAndNames("active", "author")).thenReturn(List.of(
                OpenLibraryAuthorMongoEntity.builder().authorId("OL1A").hasPhoto(true).build()));
        assertThat(useCase.findAuthor("Author")[0]).isNull();
        assertThat(useCase.findAuthor("Author")[1]).contains("OL1A");
        verifyNoInteractions(spanishTranslation);
    }
}
