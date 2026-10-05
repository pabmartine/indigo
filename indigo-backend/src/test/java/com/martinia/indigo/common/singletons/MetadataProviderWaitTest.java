package com.martinia.indigo.common.singletons;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MetadataProviderWaitTest {
    @Test void waitingDoesNotAdvanceCountersOrBlockOtherRunsAndClearsOnStop() {
        MetadataSingleton state = new MetadataSingleton();
        long authors = state.start("PARTIAL", "AUTHORS");
        state.initializeRun(authors, "authors", 20);
        long books = state.start("FULL", "BOOKS");
        state.setProviderWait(authors, "Wikipedia · Margaret Rogerson", 123456L);
        var snapshot = state.getRuns().get("PARTIAL:AUTHORS");
        assertThat(snapshot).containsEntry("waitingUntil", 123456L).containsEntry("current", 0L).containsEntry("errors", 0L);
        assertThat(state.isActive(books)).isTrue();
        assertThat(state.getRuns().get("FULL:BOOKS").get("waitingUntil")).isNull();
        state.stop("AUTHORS");
        assertThat(state.getRuns().get("PARTIAL:AUTHORS").get("waitingUntil")).isNull();
        state.setProviderWait(authors, "obsolete callback", 999L);
        assertThat(state.getRuns().get("PARTIAL:AUTHORS").get("waitingUntil")).isNull();
    }
}
