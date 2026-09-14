package com.martinia.indigo.common.config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventBusConfigurationTest {
  @Test
  void saturationRunsOnPublisherWithoutGrowingQueueOrDroppingWork() throws Exception {
    var executor = new EventBusConfiguration().eventTaskExecutor(1, 1);
    executor.initialize();
    var started = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var queuedDone = new CountDownLatch(1);
    try {
      executor.execute(() -> {
        started.countDown();
        try { release.await(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
      });
      assertTrue(started.await(5, TimeUnit.SECONDS));
      executor.execute(queuedDone::countDown);
      var executedBy = new AtomicReference<Thread>();
      executor.execute(() -> executedBy.set(Thread.currentThread()));
      assertSame(Thread.currentThread(), executedBy.get());
      assertEquals(1, executor.getThreadPoolExecutor().getQueue().size());
      release.countDown();
      assertTrue(queuedDone.await(5, TimeUnit.SECONDS));
    } finally {
      release.countDown();
      executor.shutdown();
    }
  }
}
