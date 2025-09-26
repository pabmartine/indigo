package com.martinia.indigo.common.domain.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NumBooksUnitTest {

    @Test
    void createNumBooks_WithDefaultConstructor_ShouldSetDefaultValues() {
        // When
        NumBooks numBooks = new NumBooks();

        // Then
        assertEquals(1, numBooks.getTotal());
        assertNotNull(numBooks.getLanguages());
        assertTrue(numBooks.getLanguages().isEmpty());
    }

    @Test
    void createNumBooks_WithAllArgsConstructor_ShouldSetAllProperties() {
        // Given
        int total = 25;
        Map<String, Integer> languages = new HashMap<>();
        languages.put("es", 10);
        languages.put("en", 15);

        // When
        NumBooks numBooks = new NumBooks(total, languages);

        // Then
        assertEquals(total, numBooks.getTotal());
        assertEquals(languages, numBooks.getLanguages());
        assertEquals(2, numBooks.getLanguages().size());
        assertEquals(10, numBooks.getLanguages().get("es"));
        assertEquals(15, numBooks.getLanguages().get("en"));
    }

    @Test
    void setAndGetTotal_ShouldWorkCorrectly() {
        // Given
        NumBooks numBooks = new NumBooks();
        int newTotal = 42;

        // When
        numBooks.setTotal(newTotal);

        // Then
        assertEquals(newTotal, numBooks.getTotal());
    }

    @Test
    void setAndGetLanguages_ShouldWorkCorrectly() {
        // Given
        NumBooks numBooks = new NumBooks();
        Map<String, Integer> languages = new HashMap<>();
        languages.put("fr", 5);
        languages.put("de", 8);

        // When
        numBooks.setLanguages(languages);

        // Then
        assertEquals(languages, numBooks.getLanguages());
        assertEquals(2, numBooks.getLanguages().size());
        assertEquals(5, numBooks.getLanguages().get("fr"));
        assertEquals(8, numBooks.getLanguages().get("de"));
    }

    @Test
    void setTotal_WithZero_ShouldWork() {
        // Given
        NumBooks numBooks = new NumBooks();
        int zeroTotal = 0;

        // When
        numBooks.setTotal(zeroTotal);

        // Then
        assertEquals(zeroTotal, numBooks.getTotal());
    }

    @Test
    void setTotal_WithNegativeValue_ShouldWork() {
        // Given
        NumBooks numBooks = new NumBooks();
        int negativeTotal = -5;

        // When
        numBooks.setTotal(negativeTotal);

        // Then
        assertEquals(negativeTotal, numBooks.getTotal());
    }

    @Test
    void setLanguages_WithEmptyMap_ShouldWork() {
        // Given
        NumBooks numBooks = new NumBooks();
        Map<String, Integer> emptyMap = new HashMap<>();

        // When
        numBooks.setLanguages(emptyMap);

        // Then
        assertEquals(emptyMap, numBooks.getLanguages());
        assertTrue(numBooks.getLanguages().isEmpty());
    }

    @Test
    void setLanguages_WithNull_ShouldWork() {
        // Given
        NumBooks numBooks = new NumBooks();

        // When
        numBooks.setLanguages(null);

        // Then
        assertNull(numBooks.getLanguages());
    }

    @Test
    void modifyLanguagesMap_ShouldReflectChanges() {
        // Given
        NumBooks numBooks = new NumBooks();
        Map<String, Integer> languages = new HashMap<>();
        languages.put("it", 3);
        numBooks.setLanguages(languages);

        // When
        numBooks.getLanguages().put("pt", 7);
        numBooks.getLanguages().put("it", 5); // Update existing

        // Then
        assertEquals(2, numBooks.getLanguages().size());
        assertEquals(5, numBooks.getLanguages().get("it"));
        assertEquals(7, numBooks.getLanguages().get("pt"));
    }
}