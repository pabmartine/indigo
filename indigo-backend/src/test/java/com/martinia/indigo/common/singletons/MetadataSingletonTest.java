package com.martinia.indigo.common.singletons;

import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MetadataSingletonTest {
    @Test
    void runsBooksAuthorsAndReviewsIndependently() {
        var state = new MetadataSingleton();
        long books = state.start("FULL", "BOOKS");
        long authors = state.start("PARTIAL", "AUTHORS");
        long reviews = state.start("FULL", "REVIEWS");
        state.initializeRun(books, "books", 2);
        state.initializeRun(authors, "authors", 1);
        state.initializeRun(reviews, "reviews", 1);
        state.record(authors, MetadataItemResult.FOUND);
        state.record(reviews, MetadataItemResult.ERROR);
        assertTrue(state.isActive(books));
        assertTrue(state.isRunning());
        assertFalse(state.isActive(authors));
        assertEquals(1L, state.getRuns().get("PARTIAL:AUTHORS").get("found"));
        assertEquals(1L, state.getRuns().get("FULL:REVIEWS").get("errors"));
        state.record(books, MetadataItemResult.NOT_FOUND);
        state.record(books, MetadataItemResult.SKIPPED);
        assertFalse(state.isRunning());
        assertEquals(2L, state.getRuns().get("FULL:BOOKS").get("current"));
    }

    @Test
    void replacementAndStopAffectOnlyTheirEntityAndIgnoreStaleResults() {
        var state = new MetadataSingleton();
        long books = state.start("FULL", "BOOKS");
        long oldAuthors = state.start("FULL", "AUTHORS");
        long authors = state.start("PARTIAL", "AUTHORS");
        assertFalse(state.isActive(oldAuthors));
        state.record(oldAuthors, MetadataItemResult.ERROR);
        state.complete(oldAuthors);
        assertTrue(state.isActive(authors));
        state.stop("AUTHORS");
        assertFalse(state.isActive(authors));
        assertTrue(state.isActive(books));
        assertTrue(state.isRunning());
        assertEquals(0L, state.getRuns().get("FULL:AUTHORS").get("errors"));
        state.stop();
        assertFalse(state.isRunning());
        assertFalse(state.isActive(books));
    }

    @Test
    void snapshotsCannotMutateStoredState() {
        var state = new MetadataSingleton();
        state.start("FULL", "BOOKS");
        state.getRuns().get("FULL:BOOKS").put("status", false);
        assertEquals(true, state.getRuns().get("FULL:BOOKS").get("status"));
    }
}
