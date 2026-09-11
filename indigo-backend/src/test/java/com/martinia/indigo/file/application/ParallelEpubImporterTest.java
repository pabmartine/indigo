package com.martinia.indigo.file.application;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveBookEpubFileExtractedEventUseCase;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParallelEpubImporterTest {
    @Test void parsesConcurrentlyButSerializesPersistenceAndWaitsForCompletion() throws Exception {
        var reader = mock(PreparedEpubReader.class);
        var saver = mock(SaveBookEpubFileExtractedEventUseCase.class);
        var pending = mock(PendingImportService.class);
        var progress = mock(UploadEpubFilesSingleton.class);
        when(progress.isRunning()).thenReturn(true);
        var importer = new ParallelEpubImporter();
        ReflectionTestUtils.setField(importer, "reader", reader);
        ReflectionTestUtils.setField(importer, "saver", saver);
        ReflectionTestUtils.setField(importer, "pending", pending);
        ReflectionTestUtils.setField(importer, "progress", progress);
        ReflectionTestUtils.setField(importer, "workers", 2);
        var reading = new CountDownLatch(2); var release = new CountDownLatch(1);
        when(reader.read(any())).thenAnswer(call -> {
            reading.countDown(); assertTrue(release.await(3, TimeUnit.SECONDS));
            var prepared = mock(PreparedEpubReader.Prepared.class);
            when(prepared.path()).thenReturn(call.getArgument(0)); when(prepared.opf()).thenReturn(new BookOpf());
            return prepared;
        });
        AtomicInteger writers = new AtomicInteger(), maximum = new AtomicInteger();
        doAnswer(call -> {
            maximum.accumulateAndGet(writers.incrementAndGet(), Math::max);
            Thread.sleep(10); writers.decrementAndGet(); return null;
        }).when(saver).save(any(), any());
        var executor = Executors.newSingleThreadExecutor();
        try {
            var done = executor.submit(() -> importer.process(List.of(Path.of("a.epub"), Path.of("b.epub"))));
            assertTrue(reading.await(3, TimeUnit.SECONDS)); assertFalse(done.isDone());
            release.countDown(); done.get(3, TimeUnit.SECONDS);
            verify(saver, times(2)).save(any(), any());
            verify(pending).completeBatchCategories(List.of());
            assertEquals(1, maximum.get());
        } finally { release.countDown(); executor.shutdownNow(); }
    }
}
