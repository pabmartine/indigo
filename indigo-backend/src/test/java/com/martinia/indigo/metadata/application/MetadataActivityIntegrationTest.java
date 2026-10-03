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

    @Test void protectedAndCompleteAuthorsHaveAHistoryWithAnExplicitReason() {
        var author = authorRepository.save(com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity.builder()
                .name("Author").description("Biografía").image("photo").build());
        activity.lock("AUTHORS", author.getId(), true);
        assertEquals(MetadataItemResult.SKIPPED, activity.track("AUTHORS", author.getId(), "es", () -> {
            fail("Protected author queried"); return null;
        }));
        var entry = activity.history().get(0);
        assertTrue(entry.getString("summary").contains("Protegido manualmente"));
        assertEquals("SKIPPED", entry.getString("status"));
        assertTrue(entry.get("fieldsAfter", Document.class).getBoolean("description"));
        assertTrue(entry.getList("changes", Document.class).isEmpty());
        activity.lock("AUTHORS", author.getId(), false);
        activity.track("AUTHORS", author.getId(), "es", () -> {
            ProviderDiagnostics.explain("Descripción y foto completas"); return MetadataItemResult.SKIPPED;
        });
        assertEquals(2, activity.historyPage(0, 25, "AUTHORS", "SKIPPED", null, author.getId()).getLong("total"));
    }

    @Test void paginatedHistoryIncludesOlderEntriesAndDoesNotReturnImageSnapshots() {
        var entries = new java.util.ArrayList<Document>();
        for (int i = 0; i < 125; i++) entries.add(new Document("_id", "operation-" + i).append("entityId", "entity-" + i)
                .append("type", "AUTHORS").append("status", "ERROR").append("label", "Author [literal]")
                .append("createdAt", new java.util.Date(i * 1000L)).append("events", java.util.List.of())
                .append("changes", java.util.List.of(new Document("field", "image").append("after", "base64"))));
        mongo.insert(entries, "metadataHistory");
        var page = activity.historyPage(4, 25, "AUTHORS", "ERROR", "[literal]", null);
        assertEquals(125L, page.getLong("total"));
        assertEquals(25, page.getList("items", Document.class).size());
        var item = page.getList("items", Document.class).get(0);
        assertFalse(item.containsKey("changes"));
        assertFalse(item.containsKey("events"));
        assertEquals(1, activity.historyEntry(item.getString("_id")).getList("changes", Document.class).size());
        assertThrows(ResponseStatusException.class, () -> activity.historyPage(-1, 25, null, null, null, null));
        assertThrows(ResponseStatusException.class, () -> activity.historyPage(0, 500, null, null, null, null));
        assertThrows(ResponseStatusException.class, () -> activity.historyPage(0, 25, null, "INVALID", null, null));
    }

    @Test void providerTraceAndRunningEntryArePersistedForEachOperation() {
        var book = bookRepository.save(BookMongoEntity.builder().title("Book").build());
        activity.track("BOOKS", book.getId(), "es", () -> {
            assertEquals("RUNNING", activity.history().get(0).getString("status"));
            ProviderDiagnostics.event("OPEN_LIBRARY", "Consultar libro", "NOT_FOUND", "Sin coincidencia");
            ProviderDiagnostics.event("GOOGLE_BOOKS", "Consultar libro", "FOUND", "Valoración obtenida");
            return MetadataItemResult.FOUND;
        });
        var entry = activity.history().get(0);
        assertEquals(2, entry.getList("events", Document.class).size());
        assertNotNull(entry.getDate("completedAt"));
        assertTrue(entry.getLong("durationMillis") >= 0);
        activity.track("BOOKS", book.getId(), "es", () -> MetadataItemResult.NOT_FOUND);
        assertTrue(activity.history().stream().anyMatch(item -> item.getList("events", Document.class).isEmpty()));
    }

    @Test void interruptedOperationsSurviveRestartAsErrors() {
        mongo.save(new Document("_id", "interrupted").append("status", "RUNNING").append("type", "AUTHORS")
                .append("createdAt", new java.util.Date()), "metadataHistory");
        activity.recoverInterruptedItems();
        var entry = activity.historyEntry("interrupted");
        assertEquals("ERROR", entry.getString("status"));
        assertTrue(entry.getString("summary").contains("reinicio"));
    }

    @Test @WithMockUser(authorities = "ADMIN")
    void paginatedHistoryAndDetailAreAvailableToAdministrators() throws Exception {
        mongo.save(new Document("_id", "entry").append("status", "SKIPPED").append("type", "AUTHORS"), "metadataHistory");
        mockMvc.perform(get("/api/metadata/activity/history/page").param("type", "AUTHORS"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));
        mockMvc.perform(get("/api/metadata/activity/history/entry"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SKIPPED"));
    }

    @Test @WithMockUser(authorities = "USER")
    void detailedHistoryRequiresAdministrator() throws Exception {
        mockMvc.perform(get("/api/metadata/activity/history/page")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/metadata/activity/history/entry")).andExpect(status().isForbidden());
    }
}
