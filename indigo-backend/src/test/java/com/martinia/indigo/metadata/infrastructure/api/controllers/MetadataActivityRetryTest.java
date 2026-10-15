package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.application.MetadataActivityService;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetadataActivityRetryTest {
    private final MetadataActivityService activity = mock(MetadataActivityService.class);
    private final CommandBus commands = mock(CommandBus.class);
    private final MetadataActivityController controller = new MetadataActivityController();

    private void item(String status) {
        ReflectionTestUtils.setField(controller, "activity", activity);
        ReflectionTestUtils.setField(controller, "commands", commands);
        when(activity.item("BOOKS:one")).thenReturn(new Document("status", status).append("type", "BOOKS").append("entityId", "one"));
    }

    @ParameterizedTest @ValueSource(strings = {"ERROR", "DEFERRED"})
    void incompleteBooksCanBeRetriedAndKeepTheActualResult(String status) {
        item(status);
        when(commands.executeAndWait(any(FindBookMetadataCommand.class))).thenReturn(MetadataItemResult.DEFERRED);
        assertThat(controller.retry("BOOKS:one")).isEqualTo(MetadataItemResult.DEFERRED);
        verify(commands).executeAndWait(org.mockito.ArgumentMatchers.<FindBookMetadataCommand>argThat(command -> "one".equals(command.getBookId())));
    }

    @Test void runningBookCannotBeRetried() {
        item("RUNNING");
        assertThatThrownBy(() -> controller.retry("BOOKS:one")).isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(commands);
    }
}
