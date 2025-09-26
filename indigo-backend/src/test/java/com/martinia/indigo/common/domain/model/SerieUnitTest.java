package com.martinia.indigo.common.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SerieUnitTest {

    @Test
    void createSerie_WithBuilder_ShouldSetAllProperties() {
        // Given
        int index = 1;
        String name = "Test Serie";

        // When
        Serie serie = Serie.builder()
            .index(index)
            .name(name)
            .build();

        // Then
        assertEquals(index, serie.getIndex());
        assertEquals(name, serie.getName());
    }

    @Test
    void createSerie_WithDefaultConstructor_ShouldCreateEmptySerie() {
        // When
        Serie serie = new Serie();

        // Then
        assertEquals(0, serie.getIndex());
        assertNull(serie.getName());
    }

    @Test
    void createSerie_WithAllArgsConstructor_ShouldSetAllProperties() {
        // Given
        int index = 5;
        String name = "Advanced Serie";

        // When
        Serie serie = new Serie(index, name);

        // Then
        assertEquals(index, serie.getIndex());
        assertEquals(name, serie.getName());
    }

    @Test
    void setAndGetProperties_ShouldWorkCorrectly() {
        // Given
        Serie serie = new Serie();
        int newIndex = 3;
        String newName = "Updated Serie";

        // When
        serie.setIndex(newIndex);
        serie.setName(newName);

        // Then
        assertEquals(newIndex, serie.getIndex());
        assertEquals(newName, serie.getName());
    }

    @Test
    void createSerie_WithZeroIndex_ShouldWork() {
        // Given
        int index = 0;
        String name = "Zero Index Serie";

        // When
        Serie serie = Serie.builder()
            .index(index)
            .name(name)
            .build();

        // Then
        assertEquals(index, serie.getIndex());
        assertEquals(name, serie.getName());
    }

    @Test
    void createSerie_WithNegativeIndex_ShouldWork() {
        // Given
        int index = -1;
        String name = "Negative Index Serie";

        // When
        Serie serie = Serie.builder()
            .index(index)
            .name(name)
            .build();

        // Then
        assertEquals(index, serie.getIndex());
        assertEquals(name, serie.getName());
    }

    @Test
    void createSerie_WithNullName_ShouldWork() {
        // Given
        int index = 2;
        String name = null;

        // When
        Serie serie = Serie.builder()
            .index(index)
            .name(name)
            .build();

        // Then
        assertEquals(index, serie.getIndex());
        assertNull(serie.getName());
    }

    @Test
    void createSerie_WithEmptyName_ShouldWork() {
        // Given
        int index = 4;
        String name = "";

        // When
        Serie serie = Serie.builder()
            .index(index)
            .name(name)
            .build();

        // Then
        assertEquals(index, serie.getIndex());
        assertEquals(name, serie.getName());
    }
}