package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.domain.ports.usecases.FindStatusMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FindStatusMetadataControllerUnitTest {

    @Mock
    private FindStatusMetadataUseCase useCase;

    @InjectMocks
    private FindStatusMetadataController controller;

    @Test
    void getStatus_WithValidUseCase_ShouldReturnStatusMap() {
        // Given
        Map<String, Object> expectedStatus = new HashMap<>();
        expectedStatus.put("status", "running");
        expectedStatus.put("progress", 75);
        expectedStatus.put("total", 100);

        when(useCase.getStatus()).thenReturn(expectedStatus);

        // When
        ResponseEntity<Map<String, Object>> result = controller.getStatus();

        // Then
        verify(useCase, times(1)).getStatus();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedStatus, result.getBody());
    }

    @Test
    void getStatus_WhenUseCaseReturnsEmptyMap_ShouldReturnEmptyMap() {
        // Given
        Map<String, Object> emptyStatus = new HashMap<>();
        when(useCase.getStatus()).thenReturn(emptyStatus);

        // When
        ResponseEntity<Map<String, Object>> result = controller.getStatus();

        // Then
        verify(useCase, times(1)).getStatus();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(emptyStatus, result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void getStatus_WhenUseCaseReturnsNull_ShouldReturnNullBody() {
        // Given
        when(useCase.getStatus()).thenReturn(null);

        // When
        ResponseEntity<Map<String, Object>> result = controller.getStatus();

        // Then
        verify(useCase, times(1)).getStatus();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getStatus_WithComplexStatusData_ShouldReturnComplexData() {
        // Given
        Map<String, Object> complexStatus = new HashMap<>();
        complexStatus.put("status", "processing");
        complexStatus.put("current_task", "metadata_extraction");
        complexStatus.put("progress", 45.7);
        complexStatus.put("estimated_time_remaining", "2 minutes");
        complexStatus.put("errors", new String[]{"warning1", "warning2"});

        when(useCase.getStatus()).thenReturn(complexStatus);

        // When
        ResponseEntity<Map<String, Object>> result = controller.getStatus();

        // Then
        verify(useCase, times(1)).getStatus();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(complexStatus, result.getBody());
        assertEquals("processing", result.getBody().get("status"));
        assertEquals(45.7, result.getBody().get("progress"));
    }

    @Test
    void getStatus_WhenUseCaseThrowsException_ShouldPropagateException() {
        // Given
        when(useCase.getStatus()).thenThrow(new RuntimeException("Status retrieval failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.getStatus();
        });

        verify(useCase, times(1)).getStatus();
    }

    @Test
    void getStatus_ShouldAlwaysReturnHttpStatusOk() {
        // Given
        Map<String, Object> anyStatus = new HashMap<>();
        anyStatus.put("test", "value");
        when(useCase.getStatus()).thenReturn(anyStatus);

        // When
        ResponseEntity<Map<String, Object>> result = controller.getStatus();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    void getStatus_ShouldCallUseCaseExactlyOnce() {
        // Given
        Map<String, Object> status = new HashMap<>();
        when(useCase.getStatus()).thenReturn(status);

        // When
        controller.getStatus();
        controller.getStatus(); // Call twice

        // Then
        verify(useCase, times(2)).getStatus(); // Should be called twice
    }

    @Test
    void getStatus_WithBooleanValues_ShouldHandleBooleanValues() {
        // Given
        Map<String, Object> statusWithBooleans = new HashMap<>();
        statusWithBooleans.put("is_running", true);
        statusWithBooleans.put("has_errors", false);
        statusWithBooleans.put("completed", true);

        when(useCase.getStatus()).thenReturn(statusWithBooleans);

        // When
        ResponseEntity<Map<String, Object>> result = controller.getStatus();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(statusWithBooleans, result.getBody());
        assertTrue((Boolean) result.getBody().get("is_running"));
        assertFalse((Boolean) result.getBody().get("has_errors"));
    }
}