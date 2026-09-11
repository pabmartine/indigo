package com.martinia.indigo.file.application;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.ports.usecases.events.*;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PendingImportServiceTest {
    private final MongoTemplate mongo = mock(MongoTemplate.class);
    private final UploadEpubFilesSingleton uploads = mock(UploadEpubFilesSingleton.class);
    private final MoveEpubFileEventUseCase mover = mock(MoveEpubFileEventUseCase.class);
    private final SaveAuthorEpubFileEventUseCase authors = mock(SaveAuthorEpubFileEventUseCase.class);
    private final SaveTagEpubFileEventUseCase tags = mock(SaveTagEpubFileEventUseCase.class);
    private final PendingImportService service = new PendingImportService();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "mongo", mongo);
        ReflectionTestUtils.setField(service, "uploadState", uploads);
        ReflectionTestUtils.setField(service, "uploads", "/tmp/pending-tests/uploads");
        ReflectionTestUtils.setField(service, "library", "/tmp/pending-tests/library");
        provider("mover", mover); provider("authors", authors); provider("tags", tags);
    }
    private void provider(String field, Object consumer) {
        ObjectProvider provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(consumer);
        ReflectionTestUtils.setField(service, field, provider);
    }
    private void entry(String source) {
        when(mongo.findById("book:1", Document.class, "pendingImports")).thenReturn(new Document("_id", "book:1")
                .append("bookId", "1").append("source", source).append("target", "/tmp/pending-tests/library/book"));
    }
    @Test void refusesRetryWhileImportIsRunning() {
        when(uploads.isRunning()).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.retry("1")).getStatusCode().value());
        verifyNoInteractions(mover, authors, tags, mongo);
    }
    @Test void missingImportReturnsNotFound() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.retry("missing")).getStatusCode().value());
    }
    @Test void completedTasksAreNotRepeated() {
        entry("/tmp/pending-tests/uploads/book.epub");
        when(mongo.findById(anyString(), eq(Document.class), eq("pendingImportTasks"))).thenReturn(new Document());
        assertTrue(service.retry("1").getList("pendingTasks", String.class).isEmpty());
        verifyNoInteractions(mover, authors, tags);
    }
    @Test void onlyUnfinishedTasksAreRetriedAndFailureRemainsVisible() {
        entry("/tmp/pending-tests/uploads/book.epub");
        when(mongo.findById("book:1:authorsDone", Document.class, "pendingImportTasks")).thenReturn(new Document());
        var result = service.retry("1");
        verify(mover).move(java.nio.file.Path.of("/tmp/pending-tests/uploads/book.epub"), java.nio.file.Path.of("/tmp/pending-tests/library/book"));
        verify(tags).save("1", true);
        verifyNoInteractions(authors);
        assertNotNull(result.getString("lastError"));
        assertEquals(java.util.List.of("fileDone", "tagsDone"), result.getList("pendingTasks", String.class));
    }
    @Test void rejectsPathsOutsideConfiguredRoots() {
        entry("/tmp/elsewhere/book.epub");
        assertNotNull(service.retry("1").getString("lastError"));
        verifyNoInteractions(mover, authors, tags);
    }
}
