package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.application.MetadataExecutionService;
import com.martinia.indigo.metadata.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookMetadataPreparationFailureTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void selectionAndCheckpointFailuresFinishRunWithAnError(boolean selectionFails) {
        var state = new MetadataSingleton();
        var repository = mock(BookRepository.class);
        var executions = mock(MetadataExecutionService.class);
        var useCase = new StartFillBooksMetadataUseCaseImpl();
        ReflectionTestUtils.setField(useCase, "metadataSingleton", state);
        ReflectionTestUtils.setField(useCase, "bookRepository", repository);
        ReflectionTestUtils.setField(useCase, "executions", executions);
        long runId = state.start("FULL", "BOOKS");
        if (selectionFails) when(repository.findAllBookIds()).thenThrow(new IllegalStateException("Mongo unavailable"));
        else {
            when(repository.findAllBookIds()).thenReturn(List.of());
            when(executions.pending(anyString(), anyList())).thenThrow(new IllegalStateException("Mongo unavailable"));
        }
        assertThatThrownBy(() -> useCase.start(BookMetadataScope.ALL, MetadataMergePolicy.FILL_MISSING,
                DynamicMetadataPolicy.REFRESH_IF_STALE, runId)).isInstanceOf(IllegalStateException.class);
        assertThat(state.isActive(runId)).isFalse();
        assertThat(state.isRunning()).isFalse();
        assertThat(state.getRuns().get("FULL:BOOKS")).containsEntry("errors", 1L);
    }
}
