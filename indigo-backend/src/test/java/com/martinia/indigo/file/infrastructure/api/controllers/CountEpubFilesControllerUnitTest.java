package com.martinia.indigo.file.infrastructure.api.controllers;

import com.martinia.indigo.file.domain.ports.usecases.CountEpubFilesUseCase;
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
public class CountEpubFilesControllerUnitTest {

    @Mock
    private CountEpubFilesUseCase useCase;

    @InjectMocks
    private CountEpubFilesController controller;

    @Test
    void count_ShouldReturnCountFromUseCase() {
        // Given
        Long expectedCount = 42L;
        when(useCase.count()).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> result = controller.count();

        // Then
        verify(useCase, times(1)).count();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedCount, result.getBody());
    }

    @Test
    void count_ShouldReturnZeroWhenUseCaseReturnsZero() {
        // Given
        Long zeroCount = 0L;
        when(useCase.count()).thenReturn(zeroCount);

        // When
        ResponseEntity<Long> result = controller.count();

        // Then
        verify(useCase, times(1)).count();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(zeroCount, result.getBody());
    }

    @Test
    void count_WhenUseCaseThrowsException_ShouldPropagateException() {
        // Given
        when(useCase.count()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.count();
        });

        verify(useCase, times(1)).count();
    }

    @Test
    void count_ShouldAlwaysReturnHttpStatusOk() {
        // Given
        when(useCase.count()).thenReturn(123L);

        // When
        ResponseEntity<Long> result = controller.count();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(200, result.getStatusCodeValue());
    }
}