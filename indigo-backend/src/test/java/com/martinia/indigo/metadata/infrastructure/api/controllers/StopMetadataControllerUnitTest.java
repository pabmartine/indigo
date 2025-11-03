package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.domain.ports.usecases.StopMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StopMetadataControllerUnitTest {

    @Mock
    private StopMetadataUseCase useCase;

    @InjectMocks
    private StopMetadataController controller;

    @Test
    void stop_ShouldCallUseCaseAndReturnOk() {
        // When
        ResponseEntity<Void> result = controller.stop();

        // Then
        verify(useCase, times(1)).stop();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void stop_WhenUseCaseThrowsException_ShouldPropagateException() {
        // Given
        doThrow(new RuntimeException("Test exception")).when(useCase).stop();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.stop();
        });

        verify(useCase, times(1)).stop();
    }

    @Test
    void stop_ShouldAlwaysReturnHttpStatusOk() {
        // When
        ResponseEntity<Void> result = controller.stop();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    void stop_ShouldAlwaysReturnNullBody() {
        // When
        ResponseEntity<Void> result = controller.stop();

        // Then
        assertNull(result.getBody());
    }
}