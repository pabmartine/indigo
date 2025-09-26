package com.martinia.indigo.file.infrastructure.api.controllers;

import com.martinia.indigo.file.domain.ports.usecases.FindEpubFilesUploadPathUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FindEpubFilesUploadPathControllerUnitTest {

    @Mock
    private FindEpubFilesUploadPathUseCase useCase;

    @InjectMocks
    private FindEpubFilesUploadPathController controller;

    @Test
    void findPath_ShouldReturnPathFromUseCaseInMap() {
        // Given
        String expectedPath = "/uploads/epub/files";
        when(useCase.findPath()).thenReturn(expectedPath);

        // When
        ResponseEntity<Map<String, String>> result = controller.findPath();

        // Then
        verify(useCase, times(1)).findPath();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().containsKey("path"));
        assertEquals(expectedPath, result.getBody().get("path"));
    }

    @Test
    void findPath_WithNullPathFromUseCase_ShouldReturnMapWithNullValue() {
        // Given
        when(useCase.findPath()).thenReturn(null);

        // When
        ResponseEntity<Map<String, String>> result = controller.findPath();

        // Then
        verify(useCase, times(1)).findPath();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().containsKey("path"));
        assertNull(result.getBody().get("path"));
    }

    @Test
    void findPath_WithEmptyPathFromUseCase_ShouldReturnMapWithEmptyValue() {
        // Given
        String emptyPath = "";
        when(useCase.findPath()).thenReturn(emptyPath);

        // When
        ResponseEntity<Map<String, String>> result = controller.findPath();

        // Then
        verify(useCase, times(1)).findPath();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().containsKey("path"));
        assertEquals(emptyPath, result.getBody().get("path"));
    }

    @Test
    void findPath_WhenUseCaseThrowsException_ShouldPropagateException() {
        // Given
        when(useCase.findPath()).thenThrow(new RuntimeException("Path lookup failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.findPath();
        });

        verify(useCase, times(1)).findPath();
    }

    @Test
    void findPath_ShouldAlwaysReturnHttpStatusOk() {
        // Given
        when(useCase.findPath()).thenReturn("/some/path");

        // When
        ResponseEntity<Map<String, String>> result = controller.findPath();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    void findPath_ShouldAlwaysCreateNewMapWithPathKey() {
        // Given
        String testPath = "/test/path";
        when(useCase.findPath()).thenReturn(testPath);

        // When
        ResponseEntity<Map<String, String>> result = controller.findPath();

        // Then
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertTrue(result.getBody().containsKey("path"));
        assertEquals(testPath, result.getBody().get("path"));
    }
}