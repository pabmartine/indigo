package com.martinia.indigo.metadata.application.reviews;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.*;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindReviewMetadataUseCase;
import jakarta.annotation.Resource;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewQueueIntegrationTest extends BaseIndigoIntegrationTest {
    @Resource private ReviewQueueService queue;
    @Autowired private MongoTemplate mongo;
    @MockBean private FindReviewMetadataUseCase reviews;
    @BeforeEach void cleanQueue() {
        mongo.dropCollection("reviewQueue"); mongo.dropCollection("metadataItems"); mongo.dropCollection("metadataLocks");
        mongo.dropCollection("reviewProviderStates");
    }
    private BookMongoEntity book(boolean hasReviews) {
        return bookRepository.save(BookMongoEntity.builder().title("Book").reviews(hasReviews ? List.of(new ReviewMongo()) : List.of()).build());
    }
    @Test void missingOnlyProcessesBooksWithoutReviewsAndFullRestartsAtZero() {
        var missing = book(false); var existing = book(true);
        when(reviews.find(anyString(), eq(true), eq("es"))).thenReturn(MetadataItemResult.NOT_FOUND);
        assertEquals(1L, ((Number) queue.start(false, "es", false).get("total")).longValue());
        queue.tick(); queue.tick();
        assertEquals("COMPLETED", queue.status().getString("status"));
        verify(reviews).find(missing.getId(), true, "es");
        verify(reviews, never()).find(existing.getId(), true, "es");
        var full = queue.start(true, "es", false);
        assertNull(full.get("cursor")); assertEquals(2L, ((Number) full.get("total")).longValue());
        queue.tick(); queue.tick(); queue.tick();
        assertEquals("COMPLETED", queue.status().getString("status"));
        assertEquals(2L, ((Number) queue.status().get("processed")).longValue());
    }
    @Test void pauseSurvivesServiceRecreationAndMissingCannotReplaceIt() {
        book(false); queue.start(true, "es", false); queue.control("pause");
        var recreated = new ReviewQueueService();
        ReflectionTestUtils.setField(recreated, "mongo", mongo);
        assertEquals("PAUSED", recreated.status().getString("status"));
        recreated.tick(); verifyNoInteractions(reviews);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> queue.start(false, "es", false)).getStatusCode().value());
        queue.control("resume"); assertEquals("RUNNING", queue.status().getString("status"));
        queue.control("stop"); assertEquals("STOPPED", queue.status().getString("status"));
        queue.tick(); verifyNoInteractions(reviews);
    }
    @Test void fullReplacementRequiresConfirmationAndOldRunCannotAdvanceNewCursor() {
        book(false); var original = queue.start(true, "es", false).getString("runId");
        assertThrows(ResponseStatusException.class, () -> queue.start(true, "es", false));
        when(reviews.find(anyString(), eq(true), eq("es"))).thenAnswer(call -> {
            queue.start(true, "es", true);
            return MetadataItemResult.FOUND;
        });
        queue.tick();
        assertNotEquals(original, queue.status().getString("runId"));
        assertNull(queue.status().get("cursor"));
        assertEquals(0L, ((Number) queue.status().get("processed")).longValue());
    }
    @Test void pauseInterruptsRateWaitBeforeAnotherRequestAndKeepsCursor() throws Exception {
        book(false); queue.start(true, "es", false);
        queue.configure(15, 30);
        queue.awaitPermit("amazon"); // Persist the first request, as if sent before a restart.
        CountDownLatch waiting = new CountDownLatch(1);
        when(reviews.find(anyString(), eq(true), eq("es"))).thenAnswer(call -> {
            waiting.countDown(); queue.awaitPermit("amazon"); fail("Second request must not be sent"); return null;
        });
        var executor = Executors.newSingleThreadExecutor();
        try {
            var task = executor.submit(queue::tick);
            assertTrue(waiting.await(3, TimeUnit.SECONDS));
            queue.control("pause"); task.get(3, TimeUnit.SECONDS);
            assertEquals("PAUSED", queue.status().getString("status")); assertNull(queue.status().get("cursor"));
            assertEquals(15, queue.status().get("settings", Document.class).getInteger("amazon"));
        } finally { executor.shutdownNow(); }
    }
    @Test void failedBookIsDeferredInsteadOfImmediatelyRepeated() {
        book(false); queue.start(true, "es", false);
        when(reviews.find(anyString(), eq(true), eq("es"))).thenReturn(MetadataItemResult.ERROR);
        queue.tick(); queue.tick();
        verify(reviews, times(1)).find(anyString(), eq(true), eq("es"));
        assertTrue(queue.status().getDate("nextAttempt").after(new Date()));
        assertNull(queue.status().get("cursor"));
    }
    @Test void runningQueueResumesFromPersistedCursorAfterServiceRecreation() {
        book(false); book(false);
        when(reviews.find(anyString(), eq(true), eq("es"))).thenReturn(MetadataItemResult.NOT_FOUND);
        queue.start(true, "es", false); queue.tick();
        String cursor = queue.status().getString("cursor");
        var recreated = new ReviewQueueService();
        for (String field : List.of("mongo", "reviews", "activity"))
            ReflectionTestUtils.setField(recreated, field, ReflectionTestUtils.getField(queue, field));
        recreated.tick(); recreated.tick();
        assertEquals("COMPLETED", recreated.status().getString("status"));
        verify(reviews, times(1)).find(cursor, true, "es");
        verify(reviews, times(2)).find(anyString(), eq(true), eq("es"));
    }
    @Test void finishedRequestCanCommitProgressWhileUserPauses() {
        var b = book(false); queue.start(true, "es", false);
        when(reviews.find(anyString(), eq(true), eq("es"))).thenAnswer(call -> {
            queue.control("pause"); return MetadataItemResult.NOT_FOUND;
        });
        queue.tick();
        assertEquals("PAUSED", queue.status().getString("status"));
        assertEquals(b.getId(), queue.status().getString("cursor"));
        queue.tick(); verify(reviews, times(1)).find(anyString(), eq(true), eq("es"));
    }
    @Test @WithMockUser(authorities = "ADMIN") void administratorCanConfigureAndControlQueue() throws Exception {
        book(false);
        mockMvc.perform(post("/api/metadata/review-queue/settings?amazon=15&goodreads=30")).andExpect(status().isOk());
        mockMvc.perform(post("/api/metadata/review-queue/settings?amazon=0&goodreads=30")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/metadata/review-queue/start?all=true")).andExpect(status().isOk());
        mockMvc.perform(post("/api/metadata/review-queue/start?all=false")).andExpect(status().isConflict());
        mockMvc.perform(post("/api/metadata/review-queue/pause")).andExpect(status().isOk());
        mockMvc.perform(get("/api/metadata/review-queue")).andExpect(jsonPath("$.status").value("PAUSED"));
    }
    @Test @WithMockUser(authorities = "USER") void nonAdministratorCannotReadOrChangeQueue() throws Exception {
        mockMvc.perform(get("/api/metadata/review-queue")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/metadata/review-queue/start?all=true")).andExpect(status().isForbidden());
    }
}
