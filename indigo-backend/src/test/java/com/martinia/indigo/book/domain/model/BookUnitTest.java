package com.martinia.indigo.book.domain.model;

import com.martinia.indigo.common.domain.model.Review;
import com.martinia.indigo.common.domain.model.Serie;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookUnitTest {

    @Test
    void createBook_WithBuilder_ShouldSetAllProperties() {
        // Given
        String id = "book123";
        String title = "Test Book";
        String path = "/books/test";
        String comment = "Test comment";
        String provider = "test-provider";
        Serie serie = Serie.builder().name("Test Serie").build();
        Date pubDate = new Date();
        Date lastModified = new Date();
        int pages = 300;
        float rating = 4.5f;
        String image = "test.jpg";
        List<String> authors = Arrays.asList("Author 1", "Author 2");
        List<String> tags = Arrays.asList("Fiction", "Adventure");
        List<String> similar = Arrays.asList("book1", "book2");
        List<String> recommendations = Arrays.asList("book3", "book4");
        List<String> languages = Arrays.asList("es", "en");
        List<Review> reviews = Arrays.asList(
            Review.builder().rating(4).comment("Good book").build(),
            Review.builder().rating(5).comment("Excellent").build()
        );
        Date lastMetadataSync = new Date();
        float version = 1.0f;

        // When
        Book book = Book.builder()
            .id(id)
            .title(title)
            .path(path)
            .comment(comment)
            .provider(provider)
            .serie(serie)
            .pubDate(pubDate)
            .lastModified(lastModified)
            .pages(pages)
            .rating(rating)
            .image(image)
            .authors(authors)
            .tags(tags)
            .similar(similar)
            .recommendations(recommendations)
            .languages(languages)
            .reviews(reviews)
            .lastMetadataSync(lastMetadataSync)
            .version(version)
            .build();

        // Then
        assertEquals(id, book.getId());
        assertEquals(title, book.getTitle());
        assertEquals(path, book.getPath());
        assertEquals(comment, book.getComment());
        assertEquals(provider, book.getProvider());
        assertEquals(serie, book.getSerie());
        assertEquals(pubDate, book.getPubDate());
        assertEquals(lastModified, book.getLastModified());
        assertEquals(pages, book.getPages());
        assertEquals(rating, book.getRating());
        assertEquals(image, book.getImage());
        assertEquals(authors, book.getAuthors());
        assertEquals(tags, book.getTags());
        assertEquals(similar, book.getSimilar());
        assertEquals(recommendations, book.getRecommendations());
        assertEquals(languages, book.getLanguages());
        assertEquals(reviews, book.getReviews());
        assertEquals(lastMetadataSync, book.getLastMetadataSync());
        assertEquals(version, book.getVersion());
    }

    @Test
    void createBook_WithDefaultConstructor_ShouldCreateEmptyBook() {
        // When
        Book book = new Book();

        // Then
        assertNull(book.getId());
        assertNull(book.getTitle());
        assertNull(book.getPath());
        assertNull(book.getComment());
        assertNull(book.getProvider());
        assertNull(book.getSerie());
        assertNull(book.getPubDate());
        assertNull(book.getLastModified());
        assertEquals(0, book.getPages());
        assertEquals(0.0f, book.getRating());
        assertNull(book.getImage());
        assertNull(book.getAuthors());
        assertNull(book.getTags());
        assertNull(book.getSimilar());
        assertNull(book.getRecommendations());
        assertNull(book.getLanguages());
        assertNull(book.getReviews());
        assertNull(book.getLastMetadataSync());
        assertEquals(0.0f, book.getVersion());
    }

    @Test
    void setAndGetProperties_ShouldWorkCorrectly() {
        // Given
        Book book = new Book();
        String newTitle = "Updated Title";
        int newPages = 500;

        // When
        book.setTitle(newTitle);
        book.setPages(newPages);

        // Then
        assertEquals(newTitle, book.getTitle());
        assertEquals(newPages, book.getPages());
    }

    @Test
    void createBook_WithAllArgsConstructor_ShouldSetAllProperties() {
        // Given
        String id = "book456";
        String title = "Another Test Book";
        String path = "/books/another";
        String comment = "Another comment";
        String provider = "another-provider";
        Serie serie = Serie.builder().name("Another Serie").build();
        Date pubDate = new Date();
        Date lastModified = new Date();
        int pages = 250;
        float rating = 3.8f;
        String image = "another.jpg";
        List<String> authors = Arrays.asList("Author 3");
        List<String> tags = Arrays.asList("Drama");
        List<String> similar = Arrays.asList("book5");
        List<String> recommendations = Arrays.asList("book6");
        List<String> languages = Arrays.asList("fr");
        List<Review> reviews = Arrays.asList(
            Review.builder().rating(3).comment("Ok book").build()
        );
        Date lastMetadataSync = new Date();
        float version = 2.0f;

        // When
        Book book = new Book(id, title, path, comment, provider, serie, pubDate,
            lastModified, pages, rating, image, authors, tags, similar,
            recommendations, languages, reviews, lastMetadataSync, version);

        // Then
        assertEquals(id, book.getId());
        assertEquals(title, book.getTitle());
        assertEquals(path, book.getPath());
        assertEquals(comment, book.getComment());
        assertEquals(provider, book.getProvider());
        assertEquals(serie, book.getSerie());
        assertEquals(pubDate, book.getPubDate());
        assertEquals(lastModified, book.getLastModified());
        assertEquals(pages, book.getPages());
        assertEquals(rating, book.getRating());
        assertEquals(image, book.getImage());
        assertEquals(authors, book.getAuthors());
        assertEquals(tags, book.getTags());
        assertEquals(similar, book.getSimilar());
        assertEquals(recommendations, book.getRecommendations());
        assertEquals(languages, book.getLanguages());
        assertEquals(reviews, book.getReviews());
        assertEquals(lastMetadataSync, book.getLastMetadataSync());
        assertEquals(version, book.getVersion());
    }
}