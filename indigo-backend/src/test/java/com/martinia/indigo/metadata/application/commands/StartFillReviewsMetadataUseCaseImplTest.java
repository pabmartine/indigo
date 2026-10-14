package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.application.reviews.ReviewQueueService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;

class StartFillReviewsMetadataUseCaseImplTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void delegatesToPersistentQueueWithRequestedMode(boolean all) {
        var queue = mock(ReviewQueueService.class);
        var state = mock(MetadataSingleton.class);
        var useCase = new StartFillReviewsMetadataUseCaseImpl();
        ReflectionTestUtils.setField(useCase, "reviewQueue", queue);
        ReflectionTestUtils.setField(useCase, "metadataSingleton", state);
        useCase.start(all, "en", 42);
        verify(queue).start(all, "en", false);
        verify(state).complete(42);
    }
}
