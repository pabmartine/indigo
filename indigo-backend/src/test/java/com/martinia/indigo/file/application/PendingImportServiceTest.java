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
    @Test void startupRecoveryWaitsForBatchAndResumesOnlyOnce() {
        when(uploads.isManagedProcessing()).thenReturn(true);
        service.resume();
        verifyNoInteractions(mongo, mover, authors, tags);
        when(uploads.isManagedProcessing()).thenReturn(false);
        when(mongo.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Document.class), eq("pendingImports")))
                .thenReturn(java.util.List.of());
        service.resumeAfterBatch();
        service.resumeAfterBatch();
        verify(mongo, times(1)).find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Document.class), eq("pendingImports"));
    }

    @Test void batchStartingDuringRecoveryDefersRemainingEntries() {
        when(uploads.isRunning()).thenReturn(false, true);
        when(mongo.find(any(org.springframework.data.mongodb.core.query.Query.class), eq(Document.class), eq("pendingImports")))
                .thenReturn(java.util.List.of(new Document("bookId", "1"), new Document("bookId", "2")));
        service.resume();
        verifyNoInteractions(mover, authors, tags);
        verify(mongo, never()).findById("book:2:fileDone", Document.class, "pendingImportTasks");
        when(uploads.isRunning()).thenReturn(false);
        when(mongo.findById(anyString(), eq(Document.class), eq("pendingImportTasks"))).thenReturn(new Document());
        service.resumeAfterBatch();
        verify(mongo).findById("book:2:fileDone", Document.class, "pendingImportTasks");
    }

    @Test void managedFileMovesForDifferentBooksDoNotShareAGlobalMonitor() throws Exception {
        when(mongo.findById(anyString(), eq(Document.class), eq("pendingImports"))).thenAnswer(call -> {
            String key = call.getArgument(0);
            String id = key.substring(5);
            return new Document("_id", key).append("bookId", id)
                    .append("source", "/tmp/pending-tests/uploads/" + id + ".epub")
                    .append("target", "/tmp/pending-tests/library/" + id);
        });
        var moving = new java.util.concurrent.CountDownLatch(2);
        doAnswer(call -> {
            moving.countDown();
            assertTrue(moving.await(5, java.util.concurrent.TimeUnit.SECONDS), "File moves were serialized");
            return null;
        }).when(mover).move(any(), any());
        try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> service.finishManaged("one", false));
            var second = pool.submit(() -> service.finishManaged("two", false));
            first.get(6, java.util.concurrent.TimeUnit.SECONDS);
            second.get(6, java.util.concurrent.TimeUnit.SECONDS);
        }
        verify(mover, times(2)).move(any(), any());
    }

    @Test void categoryCheckpointsUseBoundedBulkWrites() {
        var bulk = mock(org.springframework.data.mongodb.core.BulkOperations.class);
        when(mongo.bulkOps(org.springframework.data.mongodb.core.BulkOperations.BulkMode.UNORDERED, "pendingImportTasks")).thenReturn(bulk);
        var ids = java.util.stream.IntStream.range(0, 1201).mapToObj(Integer::toString).toList();
        service.completeAll(ids, "tagsDone");
        verify(bulk, times(1201)).upsert(any(org.springframework.data.mongodb.core.query.Query.class), any(org.springframework.data.mongodb.core.query.Update.class));
        verify(bulk, times(3)).execute();
    }

}
