package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import org.junit.jupiter.api.BeforeEach;
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
public class StartMetadataControllerUnitTest {

    @Mock
    private StartMetadataUseCase useCase;

    @InjectMocks
    private StartMetadataController controller;

    private String lang;
    private String type;
    private String entity;

    @BeforeEach
    void setUp() {
        lang = "en";
        type = "book";
        entity = "test-entity";
    }

    @Test
    void initialLoad_WithValidParameters_ShouldCallUseCaseAndReturnOk() {
        // Given
        // Setup is done in @BeforeEach

        // When
        ResponseEntity<Void> result = controller.initialLoad(lang, type, entity);

        // Then
        verify(useCase, times(1)).start(lang, type, entity);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void initialLoad_WithDifferentParameters_ShouldCallUseCaseWithThoseParameters() {
        // Given
        String specificLang = "es";
        String specificType = "author";
        String specificEntity = "specific-entity";

        // When
        ResponseEntity<Void> result = controller.initialLoad(specificLang, specificType, specificEntity);

        // Then
        verify(useCase, times(1)).start(specificLang, specificType, specificEntity);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void initialLoad_WithNullParameters_ShouldPassNullValuesToUseCase() {
        // Given
        String nullLang = null;
        String nullType = null;
        String nullEntity = null;

        // When
        ResponseEntity<Void> result = controller.initialLoad(nullLang, nullType, nullEntity);

        // Then
        verify(useCase, times(1)).start(nullLang, nullType, nullEntity);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void initialLoad_WithEmptyStrings_ShouldPassEmptyStringsToUseCase() {
        // Given
        String emptyLang = "";
        String emptyType = "";
        String emptyEntity = "";

        // When
        ResponseEntity<Void> result = controller.initialLoad(emptyLang, emptyType, emptyEntity);

        // Then
        verify(useCase, times(1)).start(emptyLang, emptyType, emptyEntity);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void initialLoad_WhenUseCaseThrowsException_ShouldPropagateException() {
        // Given
        doThrow(new RuntimeException("Test exception")).when(useCase).start(anyString(), anyString(), anyString());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.initialLoad(lang, type, entity);
        });

        verify(useCase, times(1)).start(lang, type, entity);
    }
}