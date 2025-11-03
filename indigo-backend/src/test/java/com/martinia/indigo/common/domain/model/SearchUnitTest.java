package com.martinia.indigo.common.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchUnitTest {

    @Test
    void createSearch_WithDefaultConstructor_ShouldCreateEmptySearch() {
        // When
        Search search = new Search();

        // Then
        assertNull(search.getTitle());
        assertNull(search.getAuthor());
        assertNull(search.getIni());
        assertNull(search.getEnd());
        assertNull(search.getMin());
        assertNull(search.getMax());
        assertNull(search.getSerie());
        assertNull(search.getSelectedTags());
        assertNull(search.getPath());
        assertNull(search.getLanguages());
    }

    @Test
    void isEmpty_WithAllNullFields_ShouldReturnTrue() {
        // Given
        Search search = new Search();

        // When
        boolean result = search.isEmpty();

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_WithTitle_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setTitle("Test Title");

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithAuthor_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setAuthor("Test Author");

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithIniDate_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setIni(new Date());

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithEndDate_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setEnd(new Date());

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithMinValue_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setMin(10);

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithMaxValue_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setMax(100);

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithSerie_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setSerie("Test Serie");

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithSelectedTags_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setSelectedTags(Arrays.asList("tag1", "tag2"));

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithPath_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setPath("/test/path");

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithEmptyStrings_ShouldReturnTrue() {
        // Given
        Search search = new Search();
        search.setTitle("");
        search.setAuthor("");
        search.setPath("");
        search.setSerie(null);

        // When
        boolean result = search.isEmpty();

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_WithEmptyList_ShouldReturnTrue() {
        // Given
        Search search = new Search();
        search.setSelectedTags(Arrays.asList());

        // When
        boolean result = search.isEmpty();

        // Then
        assertTrue(result);
    }

    @Test
    void setAndGetAllProperties_ShouldWorkCorrectly() {
        // Given
        Search search = new Search();
        String title = "Test Title";
        String author = "Test Author";
        Date ini = new Date();
        Date end = new Date();
        Integer min = 5;
        Integer max = 50;
        String serie = "Test Serie";
        List<String> selectedTags = Arrays.asList("tag1", "tag2");
        String path = "/test/path";
        List<String> languages = Arrays.asList("es", "en");

        // When
        search.setTitle(title);
        search.setAuthor(author);
        search.setIni(ini);
        search.setEnd(end);
        search.setMin(min);
        search.setMax(max);
        search.setSerie(serie);
        search.setSelectedTags(selectedTags);
        search.setPath(path);
        search.setLanguages(languages);

        // Then
        assertEquals(title, search.getTitle());
        assertEquals(author, search.getAuthor());
        assertEquals(ini, search.getIni());
        assertEquals(end, search.getEnd());
        assertEquals(min, search.getMin());
        assertEquals(max, search.getMax());
        assertEquals(serie, search.getSerie());
        assertEquals(selectedTags, search.getSelectedTags());
        assertEquals(path, search.getPath());
        assertEquals(languages, search.getLanguages());
    }

    @Test
    void isEmpty_WithMultipleFields_ShouldReturnFalse() {
        // Given
        Search search = new Search();
        search.setTitle("Test Title");
        search.setAuthor("Test Author");
        search.setMin(10);

        // When
        boolean result = search.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_WithOnlyLanguages_ShouldReturnTrue() {
        // Given - languages is not checked in isEmpty method
        Search search = new Search();
        search.setLanguages(Arrays.asList("es", "en"));

        // When
        boolean result = search.isEmpty();

        // Then
        assertTrue(result); // languages field is not considered in isEmpty method
    }
}