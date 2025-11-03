package com.martinia.indigo.metadata.application.libretranslate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TranslateLibreTranslateUseCaseImplUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TranslateLibreTranslateUseCaseImpl translateUseCase;

    private String endpoint;
    private String text;
    private String target;

    @BeforeEach
    void setUp() {
        endpoint = "https://libretranslate.example.com";
        ReflectionTestUtils.setField(translateUseCase, "endpoint", endpoint);
        text = "Hello world";
        target = "es";
    }

    @Test
    void translate_WithValidTextAndTarget_ShouldReturnTranslatedText() {
        // Given
        String expectedTranslation = "Hola mundo";
        Map<String, Object> mockResponse = new LinkedHashMap<>();
        mockResponse.put("translatedText", expectedTranslation);

        when(restTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // When
        String result = translateUseCase.translate(text, target);

        // Then
        assertEquals(expectedTranslation, result);
        verify(restTemplate, times(1)).postForObject(
            eq(endpoint + "/translate"),
            any(MultiValueMap.class),
            eq(Object.class)
        );
    }

    @Test
    void translate_WithDifferentTarget_ShouldCallCorrectEndpoint() {
        // Given
        String frenchTarget = "fr";
        String expectedTranslation = "Bonjour le monde";
        Map<String, Object> mockResponse = new LinkedHashMap<>();
        mockResponse.put("translatedText", expectedTranslation);

        when(restTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // When
        String result = translateUseCase.translate(text, frenchTarget);

        // Then
        assertEquals(expectedTranslation, result);
        verify(restTemplate).postForObject(
            eq(endpoint + "/translate"),
            any(MultiValueMap.class),
            eq(Object.class)
        );
    }

    @Test
    void translate_WhenRestTemplateThrowsException_ShouldReturnNull() {
        // Given
        when(restTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(Object.class)))
            .thenThrow(new RestClientException("Connection error"));

        // When
        String result = translateUseCase.translate(text, target);

        // Then
        assertNull(result);
        verify(restTemplate, times(1)).postForObject(anyString(), any(MultiValueMap.class), eq(Object.class));
    }

    @Test
    void translate_WithValidResponseFormat_ShouldReturnTranslatedText() {
        // Given
        String expectedTranslation = "Translated text";
        Map<String, Object> mockResponse = new LinkedHashMap<>();
        mockResponse.put("translatedText", expectedTranslation);

        when(restTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // When
        String result = translateUseCase.translate(text, target);

        // Then
        assertEquals(expectedTranslation, result);
    }

    @Test
    void translate_WithNullText_ShouldHandleGracefully() {
        // Given
        Map<String, Object> mockResponse = new LinkedHashMap<>();
        mockResponse.put("translatedText", "");

        when(restTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // When
        String result = translateUseCase.translate(null, target);

        // Then
        assertEquals("", result);
    }

    @Test
    void translate_WithEmptyText_ShouldReturnTranslation() {
        // Given
        String emptyText = "";
        String expectedTranslation = "";
        Map<String, Object> mockResponse = new LinkedHashMap<>();
        mockResponse.put("translatedText", expectedTranslation);

        when(restTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // When
        String result = translateUseCase.translate(emptyText, target);

        // Then
        assertEquals(expectedTranslation, result);
    }

    @Test
    void translate_WithNullTarget_ShouldHandleGracefully() {
        // Given
        Map<String, Object> mockResponse = new LinkedHashMap<>();
        mockResponse.put("translatedText", text);

        when(restTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // When
        String result = translateUseCase.translate(text, null);

        // Then
        assertEquals(text, result);
    }

}