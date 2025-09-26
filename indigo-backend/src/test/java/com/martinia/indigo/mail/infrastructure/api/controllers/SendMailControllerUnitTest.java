package com.martinia.indigo.mail.infrastructure.api.controllers;

import com.martinia.indigo.mail.domain.ports.usecases.SendMailUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SendMailControllerUnitTest {

    @Mock
    private SendMailUseCase useCase;

    @InjectMocks
    private SendMailController controller;

    @Test
    void send_WithValidParametersAndNoError_ShouldReturnOk() throws Exception {
        // Given
        String testPath = "/test/path";
        String testAddress = "test@example.com";
        when(useCase.mail(testPath, testAddress)).thenReturn(null);

        // When
        ResponseEntity<Void> result = controller.send(testPath, testAddress);

        // Then
        verify(useCase, times(1)).mail(testPath, testAddress);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void send_WithValidParametersAndEmptyError_ShouldReturnOk() throws Exception {
        // Given
        String testPath = "/test/path";
        String testAddress = "test@example.com";
        when(useCase.mail(testPath, testAddress)).thenReturn("");

        // When
        ResponseEntity<Void> result = controller.send(testPath, testAddress);

        // Then
        verify(useCase, times(1)).mail(testPath, testAddress);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void send_WithErrorFromUseCase_ShouldThrowException() {
        // Given
        String testPath = "/test/path";
        String testAddress = "test@example.com";
        String errorMessage = "Email sending failed";
        when(useCase.mail(testPath, testAddress)).thenReturn(errorMessage);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            controller.send(testPath, testAddress);
        });

        assertEquals(errorMessage, exception.getMessage());
        verify(useCase, times(1)).mail(testPath, testAddress);
    }

    @Test
    void send_WithDifferentParameters_ShouldCallUseCaseWithCorrectParameters() throws Exception {
        // Given
        String differentPath = "/different/path";
        String differentAddress = "different@example.com";
        when(useCase.mail(differentPath, differentAddress)).thenReturn(null);

        // When
        ResponseEntity<Void> result = controller.send(differentPath, differentAddress);

        // Then
        verify(useCase, times(1)).mail(differentPath, differentAddress);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void send_WhenUseCaseThrowsRuntimeException_ShouldPropagateException() {
        // Given
        String testPath = "/test/path";
        String testAddress = "test@example.com";
        when(useCase.mail(testPath, testAddress)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.send(testPath, testAddress);
        });

        verify(useCase, times(1)).mail(testPath, testAddress);
    }

    @Test
    void send_WithBlankErrorMessage_ShouldReturnOk() throws Exception {
        // Given
        String testPath = "/test/path";
        String testAddress = "test@example.com";
        when(useCase.mail(testPath, testAddress)).thenReturn("   ");

        // When
        ResponseEntity<Void> result = controller.send(testPath, testAddress);

        // Then
        verify(useCase, times(1)).mail(testPath, testAddress);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void send_WithNonEmptyErrorMessage_ShouldThrowExceptionWithMessage() {
        // Given
        String testPath = "/test/path";
        String testAddress = "test@example.com";
        String specificError = "File not found at path";
        when(useCase.mail(testPath, testAddress)).thenReturn(specificError);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            controller.send(testPath, testAddress);
        });

        assertEquals(specificError, exception.getMessage());
        verify(useCase, times(1)).mail(testPath, testAddress);
    }
}