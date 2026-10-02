package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorPort;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorCatalogPort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class AuthorProviderFailureTest {
    @Test
    void failedLocalLookupOrTranslationDoesNotMarkAuthorAsSynced() {
        var repository = mock(AuthorRepository.class);
        var openLibrary = mock(FindOpenLibraryAuthorPort.class);
        var catalog = mock(FindOpenLibraryAuthorCatalogPort.class);
        var wikipedia = mock(FindWikipediaAuthorPort.class);
        var author = AuthorMongoEntity.builder().id("author").name("Olivia Dean").build();
        when(repository.findById("author")).thenReturn(Optional.of(author));
        when(openLibrary.findAuthor(anyString())).thenThrow(new IllegalStateException("HTTP 429"));
        var useCase = new FindAuthorMetadataUseCaseImpl();
        ReflectionTestUtils.setField(useCase, "authorRepository", repository);
        ReflectionTestUtils.setField(useCase, "findOpenLibraryAuthorPort", Optional.of(openLibrary));
        ReflectionTestUtils.setField(useCase, "findOpenLibraryAuthorCatalogPort", Optional.of(catalog));
        ReflectionTestUtils.setField(useCase, "findWikipediaAuthorPort", Optional.of(wikipedia));
        when(catalog.findAuthor(anyString())).thenReturn(null);
        when(wikipedia.findAuthor(anyString(), anyString(), anyInt())).thenReturn(null);
        assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.ERROR);
        assertThat(author.getLastMetadataSync()).isNull();
        verify(repository, never()).save(any());
    }
}
