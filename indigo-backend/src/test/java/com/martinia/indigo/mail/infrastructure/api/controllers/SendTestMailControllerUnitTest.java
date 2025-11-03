package com.martinia.indigo.mail.infrastructure.api.controllers;

import com.martinia.indigo.mail.domain.ports.usecases.SendTestMailUseCase;
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
public class SendTestMailControllerUnitTest {

    @Mock
    private SendTestMailUseCase useCase;

    @InjectMocks
    private SendTestMailController controller;

    @Test
    void test_WithValidAddress_ShouldCallUseCaseAndReturnOk() {
        // Given
        String testAddress = "test@example.com";
        doNothing().when(useCase).test(testAddress);

        // When
        ResponseEntity<Void> result = controller.test(testAddress);

        // Then
        verify(useCase, times(1)).test(testAddress);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void test_WithDifferentAddress_ShouldCallUseCaseWithCorrectAddress() {
        // Given
        String anotherAddress = "another@example.com";
        doNothing().when(useCase).test(anotherAddress);

        // When
        ResponseEntity<Void> result = controller.test(anotherAddress);

        // Then
        verify(useCase, times(1)).test(anotherAddress);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void test_WithEmptyAddress_ShouldPassEmptyStringToUseCase() {
        // Given
        String emptyAddress = "";
        doNothing().when(useCase).test(emptyAddress);

        // When
        ResponseEntity<Void> result = controller.test(emptyAddress);

        // Then
        verify(useCase, times(1)).test(emptyAddress);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void test_WhenUseCaseThrowsException_ShouldPropagateException() {
        // Given
        String testAddress = "test@example.com";
        doThrow(new RuntimeException("Email sending failed")).when(useCase).test(testAddress);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.test(testAddress);
        });

        verify(useCase, times(1)).test(testAddress);
    }

    @Test
    void test_ShouldAlwaysReturnHttpStatusOk() {
        // Given
        String testAddress = "test@example.com";
        doNothing().when(useCase).test(anyString());

        // When
        ResponseEntity<Void> result = controller.test(testAddress);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    void test_ShouldAlwaysReturnNullBody() {
        // Given
        String testAddress = "test@example.com";
        doNothing().when(useCase).test(anyString());

        // When
        ResponseEntity<Void> result = controller.test(testAddress);

        // Then
        assertNull(result.getBody());
    }
}