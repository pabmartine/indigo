package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorPort;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorCatalogPort;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorMetadataProvenanceTest {
    @Test void biographyAndImageUseLocalOpenLibraryProvenance() {
        var repository = mock(AuthorRepository.class);
        var openLibrary = mock(FindOpenLibraryAuthorPort.class);
        var catalog = mock(FindOpenLibraryAuthorCatalogPort.class);
        var images = mock(ImageUtils.class);
        var author = AuthorMongoEntity.builder().id("author").name("Author").build();
        var useCase = new FindAuthorMetadataUseCaseImpl();
        ReflectionTestUtils.setField(useCase, "authorRepository", repository);
        ReflectionTestUtils.setField(useCase, "findOpenLibraryAuthorPort", Optional.of(openLibrary));
        ReflectionTestUtils.setField(useCase, "findOpenLibraryAuthorCatalogPort", Optional.of(catalog));
        ReflectionTestUtils.setField(useCase, "findWikipediaAuthorPort", Optional.empty());
        ReflectionTestUtils.setField(useCase, "imageUtils", images);
        when(repository.findById("author")).thenReturn(Optional.of(author));
        when(catalog.findAuthor("Author")).thenReturn(new String[]{"Biografía", "image", "OPEN_LIBRARY"});
        when(images.getBase64Url("image")).thenReturn("base64");
        useCase.find("author", false, 0, "es");
        assertEquals("Biografía", author.getDescription());
        assertEquals("OPEN_LIBRARY", author.getMetadataSources().get("description"));
        assertEquals("OPEN_LIBRARY", author.getMetadataSources().get("image"));
    }
}
