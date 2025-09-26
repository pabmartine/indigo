package com.martinia.indigo.common.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ViewUnitTest {

    @Test
    void createView_WithDefaultConstructor_ShouldCreateEmptyView() {
        // When
        View view = new View();

        // Then
        assertNull(view.getId());
        assertNull(view.getUser());
        assertNull(view.getBook());
        assertNull(view.getViewDate());
    }

    @Test
    void createView_WithAllArgsConstructor_ShouldSetAllProperties() {
        // Given
        String id = "view123";
        String user = "user123";
        String book = "book123";
        Date viewDate = new Date();

        // When
        View view = new View(id, user, book, viewDate);

        // Then
        assertEquals(id, view.getId());
        assertEquals(user, view.getUser());
        assertEquals(book, view.getBook());
        assertEquals(viewDate, view.getViewDate());
    }

    @Test
    void createView_WithBookAndUserConstructor_ShouldSetBookUserAndCurrentDate() {
        // Given
        String book = "book456";
        String user = "user456";
        Date beforeCreation = new Date();

        // When
        View view = new View(book, user);

        // Then
        assertEquals(book, view.getBook());
        assertEquals(user, view.getUser());
        assertNotNull(view.getViewDate());
        assertNull(view.getId());

        // Check that viewDate is approximately current time (within 1 second)
        Date afterCreation = new Date();
        assertTrue(view.getViewDate().getTime() >= beforeCreation.getTime());
        assertTrue(view.getViewDate().getTime() <= afterCreation.getTime());
    }

    @Test
    void setAndGetId_ShouldWorkCorrectly() {
        // Given
        View view = new View();
        String newId = "newId123";

        // When
        view.setId(newId);

        // Then
        assertEquals(newId, view.getId());
    }

    @Test
    void setAndGetUser_ShouldWorkCorrectly() {
        // Given
        View view = new View();
        String newUser = "newUser123";

        // When
        view.setUser(newUser);

        // Then
        assertEquals(newUser, view.getUser());
    }

    @Test
    void setAndGetBook_ShouldWorkCorrectly() {
        // Given
        View view = new View();
        String newBook = "newBook123";

        // When
        view.setBook(newBook);

        // Then
        assertEquals(newBook, view.getBook());
    }

    @Test
    void setAndGetViewDate_ShouldWorkCorrectly() {
        // Given
        View view = new View();
        Date newViewDate = new Date();

        // When
        view.setViewDate(newViewDate);

        // Then
        assertEquals(newViewDate, view.getViewDate());
    }

    @Test
    void setProperties_WithNullValues_ShouldWork() {
        // Given
        View view = new View("book", "user");

        // When
        view.setId(null);
        view.setUser(null);
        view.setBook(null);
        view.setViewDate(null);

        // Then
        assertNull(view.getId());
        assertNull(view.getUser());
        assertNull(view.getBook());
        assertNull(view.getViewDate());
    }

    @Test
    void createView_WithBookAndUserConstructor_WithNullValues_ShouldSetNullValues() {
        // Given
        String book = null;
        String user = null;

        // When
        View view = new View(book, user);

        // Then
        assertNull(view.getBook());
        assertNull(view.getUser());
        assertNotNull(view.getViewDate()); // Date should still be set
        assertNull(view.getId());
    }

    @Test
    void updateViewProperties_ShouldReflectChanges() {
        // Given
        View view = new View("originalBook", "originalUser");
        String updatedBook = "updatedBook";
        String updatedUser = "updatedUser";

        // When
        view.setBook(updatedBook);
        view.setUser(updatedUser);

        // Then
        assertEquals(updatedBook, view.getBook());
        assertEquals(updatedUser, view.getUser());
        assertNotEquals("originalBook", view.getBook());
        assertNotEquals("originalUser", view.getUser());
    }
}