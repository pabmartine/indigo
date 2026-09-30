package com.martinia.indigo.file.application;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveBookEpubFileExtractedEventUseCase;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParallelEpubImporterTest {
    private final PreparedEpubReader reader = mock(PreparedEpubReader.class);
    private final SaveBookEpubFileExtractedEventUseCase saver = mock(SaveBookEpubFileExtractedEventUseCase.class);
    private final PendingImportService pending = mock(PendingImportService.class);
    private final UploadEpubFilesSingleton progress = mock(UploadEpubFilesSingleton.class);
    private final ParallelEpubImporter importer = new ParallelEpubImporter();

    @BeforeEach void setup() throws Exception {
        when(progress.isRunning()).thenReturn(true);
        ReflectionTestUtils.setField(importer, "reader", reader);
        ReflectionTestUtils.setField(importer, "saver", saver);
        ReflectionTestUtils.setField(importer, "pending", pending);
        ReflectionTestUtils.setField(importer, "progress", progress);
        ReflectionTestUtils.setField(importer, "workers", 8);
        when(reader.read(any())).thenAnswer(call -> {
            var prepared = mock(PreparedEpubReader.Prepared.class);
            Path path = call.getArgument(0);
            when(prepared.path()).thenReturn(path);
            when(prepared.opf()).thenReturn(BookOpf.builder().title(path.toString()).build());
            return prepared;
        });
    }

    @Test void allEightWorkersCanSaveAndCompleteUnrelatedBooksConcurrently() throws Exception {
        var saving = new CountDownLatch(8);
        var completing = new CountDownLatch(8);
        var release = new CountDownLatch(1);
        doAnswer(call -> {
            saving.countDown();
            assertTrue(saving.await(5, TimeUnit.SECONDS), "A global save lock prevents parallel writes");
            ImportExecution.capture(EpubFileAddedEvent.builder().bookId(call.getArgument(1).toString()).build());
            return null;
        }).when(saver).save(any(), any());
        when(pending.finishManaged(anyString(), eq(false))).thenAnswer(call -> {
            completing.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return new Document("pendingTasks", List.of("tagsDone"));
        });
        try (var executor = Executors.newSingleThreadExecutor()) {
            var files = java.util.stream.IntStream.range(0, 8).mapToObj(i -> Path.of("book" + i + ".epub")).toList();
            var done = executor.submit(() -> importer.process(files));
            try {
                assertTrue(completing.await(5, TimeUnit.SECONDS));
                assertFalse(done.isDone(), "Must wait for file/author completion before finishing the batch");
            } finally { release.countDown(); }
            done.get(5, TimeUnit.SECONDS);
            verify(saver, times(8)).save(any(), any());
            verify(pending).completeBatchCategories(argThat(ids -> ids.size() == 8));
        }
    }

    @Test void versionsOfTheSameIdentityStaySerializedThroughFileCompletion() throws Exception {
        doAnswer(call -> {
            var prepared = mock(PreparedEpubReader.Prepared.class);
            when(prepared.path()).thenReturn(call.getArgument(0));
            when(prepared.opf()).thenReturn(BookOpf.builder().title("Same book").build());
            return prepared;
        }).when(reader).read(any());
        AtomicInteger active = new AtomicInteger(), maximum = new AtomicInteger();
        doAnswer(call -> {
            maximum.accumulateAndGet(active.incrementAndGet(), Math::max);
            ImportExecution.capture(EpubFileAddedEvent.builder().bookId("same").build());
            return null;
        }).when(saver).save(any(), any());
        when(pending.finishManaged("same", false)).thenAnswer(call -> {
            Thread.sleep(20);
            active.decrementAndGet();
            return new Document("pendingTasks", List.of("tagsDone"));
        });
        importer.process(List.of(Path.of("v1.epub"), Path.of("v2.epub"), Path.of("v3.epub")));
        assertEquals(1, maximum.get());
        assertEquals(0, active.get());
    }
}
