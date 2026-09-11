package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import jakarta.annotation.Resource;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MetadataActivityIntegrationTest extends BaseIndigoIntegrationTest {
    @Resource private MetadataActivityService activity;
    @org.springframework.beans.factory.annotation.Autowired private MongoTemplate mongo;
    @Resource private com.martinia.indigo.book.domain.ports.usecases.EditBookUseCase editor;
    @Resource private com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper bookMapper;

    @Test void manualEditsAreProtectedAndCanBeUnlockedFromActivity() {
        var book = bookRepository.save(BookMongoEntity.builder().title("Book").image("cover").build());
        var edit = bookMapper.entity2Domain(book);
        edit.setTitle("Manual title");
        editor.edit(edit);
        assertTrue(activity.isLocked("BOOKS", book.getId()));
        assertTrue(activity.items().stream().anyMatch(item -> book.getId().equals(item.getString("entityId")) && item.getBoolean("locked")));
        activity.lock("BOOKS", book.getId(), false);
        assertFalse(activity.isLocked("BOOKS", book.getId()));
    }
    @BeforeEach void clearActivity() {
        mongo.dropCollection("metadataHistory");
        mongo.dropCollection("metadataItems");
        mongo.dropCollection("metadataLocks");
    }
    @Test void historyUndoAndLocksPreservePersonalData() {
        var book = bookRepository.save(BookMongoEntity.builder().title("Book").rating(2).comment("Personal description").build());
        assertEquals(MetadataItemResult.FOUND, activity.track("BOOKS", book.getId(), "es", () -> {
            book.setRating(4); bookRepository.save(book); return MetadataItemResult.FOUND;
        }));
        var history = activity.history();
        assertEquals(1, history.size());
        activity.undo(history.get(0).getString("_id"));
        assertEquals(2, bookRepository.findById(book.getId()).orElseThrow().getRating());
        assertEquals("Personal description", bookRepository.findById(book.getId()).orElseThrow().getComment());
        activity.lock("BOOKS", book.getId(), true);
        assertEquals(MetadataItemResult.SKIPPED, activity.track("BOOKS", book.getId(), "es", () -> { fail("Locked item executed"); return null; }));
    }
    @Test void undoRejectsSubsequentEdits() {
        var book = bookRepository.save(BookMongoEntity.builder().title("Book").rating(2).build());
        activity.track("BOOKS", book.getId(), "es", () -> {
            book.setRating(4); bookRepository.save(book); return MetadataItemResult.FOUND;
        });
        String historyId = activity.history().get(0).getString("_id");
        book.setRating(5); bookRepository.save(book);
        assertThrows(ResponseStatusException.class, () -> activity.undo(historyId));
        assertEquals(5, bookRepository.findById(book.getId()).orElseThrow().getRating());
    }
    @Test @WithMockUser(authorities = "ADMIN")
    void administratorCanInspectHistory() throws Exception {
        mockMvc.perform(get("/api/metadata/activity")).andExpect(status().isOk());
        mockMvc.perform(get("/api/metadata/activity/history")).andExpect(status().isOk());
    }
    @Test @WithMockUser(authorities = "USER")
    void nonAdministratorCannotUndo() throws Exception {
        mockMvc.perform(post("/api/metadata/activity/undo/anything")).andExpect(status().isForbidden());
    }
    @Test void failedActionIsRetryableAndPersisted() {
        var book = bookRepository.save(BookMongoEntity.builder().title("Book").build());
        assertEquals(MetadataItemResult.ERROR, activity.track("BOOKS", book.getId(), "es", () -> { throw new IllegalStateException("Unavailable"); }));
        assertEquals("ERROR", activity.items().get(0).getString("status"));
        assertTrue(activity.items().get(0).getString("error").contains("INDIGO"));
        assertEquals("PROVIDER_ERROR", activity.items().get(0).getList("diagnostics", Document.class).get(0).getString("code"));
    }
    @Test void providerWarningsSurviveSuccessfulFallbackAndDoNotLeakIntoNextRun() {
        var book = bookRepository.save(BookMongoEntity.builder().title("Book").build());
        activity.track("BOOKS", book.getId(), "es", () -> {
            ProviderDiagnostics.record("OPEN_LIBRARY", "Obtener libro", new RuntimeException(new java.net.SocketTimeoutException("secret URL")));
            return MetadataItemResult.FOUND;
        });
        var item = activity.items().get(0);
        assertEquals("FOUND", item.getString("status"));
        assertEquals("TIMEOUT", item.getList("diagnostics", Document.class).get(0).getString("code"));
        assertFalse(item.toJson().contains("secret URL"));
        activity.track("BOOKS", book.getId(), "es", () -> MetadataItemResult.NOT_FOUND);
        assertTrue(activity.items().get(0).getList("diagnostics", Document.class).isEmpty());
    }
}
