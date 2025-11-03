package com.martinia.indigo.common.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewUnitTest {

    @Test
    void createReview_WithBuilder_ShouldSetAllProperties() {
        // Given
        String name = "Test Reviewer";
        String title = "Great Book";
        String comment = "This is an excellent book with great characters.";
        int rating = 5;
        Date date = new Date();
        Date lastMetadataSync = new Date();
        String provider = "goodreads";

        // When
        Review review = Review.builder()
            .name(name)
            .title(title)
            .comment(comment)
            .rating(rating)
            .date(date)
            .lastMetadataSync(lastMetadataSync)
            .provider(provider)
            .build();

        // Then
        assertEquals(name, review.getName());
        assertEquals(title, review.getTitle());
        assertEquals(comment, review.getComment());
        assertEquals(rating, review.getRating());
        assertEquals(date, review.getDate());
        assertEquals(lastMetadataSync, review.getLastMetadataSync());
        assertEquals(provider, review.getProvider());
    }

    @Test
    void createReview_WithDefaultConstructor_ShouldCreateEmptyReview() {
        // When
        Review review = new Review();

        // Then
        assertNull(review.getName());
        assertNull(review.getTitle());
        assertNull(review.getComment());
        assertEquals(0, review.getRating());
        assertNull(review.getDate());
        assertNull(review.getLastMetadataSync());
        assertNull(review.getProvider());
    }

    @Test
    void createReview_WithAllArgsConstructor_ShouldSetAllProperties() {
        // Given
        String name = "Another Reviewer";
        String title = "Good Book";
        String comment = "Pretty good read.";
        int rating = 4;
        Date date = new Date();
        Date lastMetadataSync = new Date();
        String provider = "amazon";

        // When
        Review review = new Review(name, title, comment, rating, date, lastMetadataSync, provider);

        // Then
        assertEquals(name, review.getName());
        assertEquals(title, review.getTitle());
        assertEquals(comment, review.getComment());
        assertEquals(rating, review.getRating());
        assertEquals(date, review.getDate());
        assertEquals(lastMetadataSync, review.getLastMetadataSync());
        assertEquals(provider, review.getProvider());
    }

    @Test
    void setAndGetProperties_ShouldWorkCorrectly() {
        // Given
        Review review = new Review();
        String newName = "Updated Reviewer";
        int newRating = 3;

        // When
        review.setName(newName);
        review.setRating(newRating);

        // Then
        assertEquals(newName, review.getName());
        assertEquals(newRating, review.getRating());
    }

    @Test
    void createReview_WithMinimalRating_ShouldWork() {
        // Given
        int rating = 1;
        String comment = "Not great";

        // When
        Review review = Review.builder()
            .rating(rating)
            .comment(comment)
            .build();

        // Then
        assertEquals(rating, review.getRating());
        assertEquals(comment, review.getComment());
        assertNull(review.getName());
        assertNull(review.getTitle());
    }

    @Test
    void createReview_WithMaximalRating_ShouldWork() {
        // Given
        int rating = 5;
        String comment = "Excellent!";

        // When
        Review review = Review.builder()
            .rating(rating)
            .comment(comment)
            .build();

        // Then
        assertEquals(rating, review.getRating());
        assertEquals(comment, review.getComment());
    }

    @Test
    void createReview_WithZeroRating_ShouldWork() {
        // Given
        int rating = 0;
        String comment = "No rating";

        // When
        Review review = Review.builder()
            .rating(rating)
            .comment(comment)
            .build();

        // Then
        assertEquals(rating, review.getRating());
        assertEquals(comment, review.getComment());
    }

    @Test
    void createReview_WithNegativeRating_ShouldWork() {
        // Given
        int rating = -1;
        String comment = "Invalid rating";

        // When
        Review review = Review.builder()
            .rating(rating)
            .comment(comment)
            .build();

        // Then
        assertEquals(rating, review.getRating());
        assertEquals(comment, review.getComment());
    }

    @Test
    void updateReviewProperties_ShouldReflectChanges() {
        // Given
        Review review = new Review();
        String originalTitle = "Original Title";
        String updatedTitle = "Updated Title";

        // When
        review.setTitle(originalTitle);
        assertEquals(originalTitle, review.getTitle());

        review.setTitle(updatedTitle);

        // Then
        assertEquals(updatedTitle, review.getTitle());
        assertNotEquals(originalTitle, review.getTitle());
    }
}