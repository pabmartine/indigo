package com.martinia.indigo.author.domain.model;

import com.martinia.indigo.common.domain.model.NumBooks;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorUnitTest {

    @Test
    void createAuthor_WithBuilder_ShouldSetAllProperties() {
        // Given
        String id = "author123";
        String name = "Test Author";
        String sort = "Author, Test";
        String description = "Test author description";
        String provider = "test-provider";
        String image = "author.jpg";
        NumBooks numBooks = new NumBooks(10, new java.util.HashMap<>());
        Date lastMetadataSync = new Date();

        // When
        Author author = Author.builder()
            .id(id)
            .name(name)
            .sort(sort)
            .description(description)
            .provider(provider)
            .image(image)
            .numBooks(numBooks)
            .lastMetadataSync(lastMetadataSync)
            .build();

        // Then
        assertEquals(id, author.getId());
        assertEquals(name, author.getName());
        assertEquals(sort, author.getSort());
        assertEquals(description, author.getDescription());
        assertEquals(provider, author.getProvider());
        assertEquals(image, author.getImage());
        assertEquals(numBooks, author.getNumBooks());
        assertEquals(lastMetadataSync, author.getLastMetadataSync());
    }

    @Test
    void createAuthor_WithDefaultConstructor_ShouldCreateEmptyAuthor() {
        // When
        Author author = new Author();

        // Then
        assertNull(author.getId());
        assertNull(author.getName());
        assertNull(author.getSort());
        assertNull(author.getDescription());
        assertNull(author.getProvider());
        assertNull(author.getImage());
        assertNull(author.getNumBooks());
        assertNull(author.getLastMetadataSync());
    }

    @Test
    void setAndGetProperties_ShouldWorkCorrectly() {
        // Given
        Author author = new Author();
        String newName = "Updated Author Name";
        String newDescription = "Updated description";

        // When
        author.setName(newName);
        author.setDescription(newDescription);

        // Then
        assertEquals(newName, author.getName());
        assertEquals(newDescription, author.getDescription());
    }

    @Test
    void createAuthor_WithAllArgsConstructor_ShouldSetAllProperties() {
        // Given
        String id = "author456";
        String name = "Another Author";
        String sort = "Author, Another";
        String description = "Another author description";
        String provider = "another-provider";
        String image = "another_author.jpg";
        NumBooks numBooks = new NumBooks(25, new java.util.HashMap<>());
        Date lastMetadataSync = new Date();

        // When
        Author author = new Author(id, name, sort, description, provider, image, numBooks, lastMetadataSync);

        // Then
        assertEquals(id, author.getId());
        assertEquals(name, author.getName());
        assertEquals(sort, author.getSort());
        assertEquals(description, author.getDescription());
        assertEquals(provider, author.getProvider());
        assertEquals(image, author.getImage());
        assertEquals(numBooks, author.getNumBooks());
        assertEquals(lastMetadataSync, author.getLastMetadataSync());
    }

    @Test
    void createAuthor_WithMinimalData_ShouldWork() {
        // Given
        String name = "Minimal Author";

        // When
        Author author = Author.builder()
            .name(name)
            .build();

        // Then
        assertNull(author.getId());
        assertEquals(name, author.getName());
        assertNull(author.getSort());
        assertNull(author.getDescription());
        assertNull(author.getProvider());
        assertNull(author.getImage());
        assertNull(author.getNumBooks());
        assertNull(author.getLastMetadataSync());
    }

    @Test
    void setNumBooks_ShouldUpdateCorrectly() {
        // Given
        Author author = new Author();
        NumBooks originalNumBooks = new NumBooks(5, new java.util.HashMap<>());
        NumBooks updatedNumBooks = new NumBooks(15, new java.util.HashMap<>());

        // When
        author.setNumBooks(originalNumBooks);
        assertEquals(originalNumBooks, author.getNumBooks());

        author.setNumBooks(updatedNumBooks);

        // Then
        assertEquals(updatedNumBooks, author.getNumBooks());
        assertNotEquals(originalNumBooks, author.getNumBooks());
    }
}