package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.infrastructure.api.mappers.AuthorDtoMapper;
import com.martinia.indigo.author.infrastructure.api.model.AuthorDto;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshAuthorMetadataUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshAuthorMetadataControllerUnitTest {

    @Mock
    private RefreshAuthorMetadataUseCase useCase;

    @Mock
    private AuthorDtoMapper mapper;

    @InjectMocks
    private RefreshAuthorMetadataController controller;

    private String lang;
    private String author;
    private Author domainAuthor;
    private AuthorDto authorDto;

    @BeforeEach
    void setUp() {
        lang = "en";
        author = "test-author";

        domainAuthor = new Author();
        domainAuthor.setName("Test Author");
        domainAuthor.setId("author123");

        authorDto = new AuthorDto();
        authorDto.setName("Test Author");
        authorDto.setId("author123");
    }

    @Test
    void refreshAuthor_WithValidParameters_ShouldReturnAuthorDto() {
        // Given
        when(useCase.findAuthorMetadata(author, lang)).thenReturn(Optional.of(domainAuthor));
        when(mapper.domain2Dto(domainAuthor)).thenReturn(authorDto);

        // When
        ResponseEntity<AuthorDto> result = controller.refreshAuthor(lang, author);

        // Then
        verify(useCase, times(1)).findAuthorMetadata(author, lang);
        verify(mapper, times(1)).domain2Dto(domainAuthor);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(authorDto, result.getBody());
    }

    @Test
    void refreshAuthor_WhenAuthorNotFound_ShouldReturnNullBody() {
        // Given
        when(useCase.findAuthorMetadata(author, lang)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AuthorDto> result = controller.refreshAuthor(lang, author);

        // Then
        verify(useCase, times(1)).findAuthorMetadata(author, lang);
        verify(mapper, never()).domain2Dto(any());
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void refreshAuthor_WithDifferentLanguage_ShouldCallUseCaseWithCorrectParameters() {
        // Given
        String spanishLang = "es";
        when(useCase.findAuthorMetadata(author, spanishLang)).thenReturn(Optional.of(domainAuthor));
        when(mapper.domain2Dto(domainAuthor)).thenReturn(authorDto);

        // When
        ResponseEntity<AuthorDto> result = controller.refreshAuthor(spanishLang, author);

        // Then
        verify(useCase, times(1)).findAuthorMetadata(author, spanishLang);
        verify(mapper, times(1)).domain2Dto(domainAuthor);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(authorDto, result.getBody());
    }

    @Test
    void refreshAuthor_WithDifferentAuthor_ShouldCallUseCaseWithCorrectParameters() {
        // Given
        String differentAuthor = "different-author";
        when(useCase.findAuthorMetadata(differentAuthor, lang)).thenReturn(Optional.of(domainAuthor));
        when(mapper.domain2Dto(domainAuthor)).thenReturn(authorDto);

        // When
        ResponseEntity<AuthorDto> result = controller.refreshAuthor(lang, differentAuthor);

        // Then
        verify(useCase, times(1)).findAuthorMetadata(differentAuthor, lang);
        verify(mapper, times(1)).domain2Dto(domainAuthor);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(authorDto, result.getBody());
    }

    @Test
    void refreshAuthor_WithNullParameters_ShouldPassNullToUseCase() {
        // Given
        String nullLang = null;
        String nullAuthor = null;
        when(useCase.findAuthorMetadata(nullAuthor, nullLang)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AuthorDto> result = controller.refreshAuthor(nullLang, nullAuthor);

        // Then
        verify(useCase, times(1)).findAuthorMetadata(nullAuthor, nullLang);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void refreshAuthor_WithEmptyStrings_ShouldPassEmptyStringsToUseCase() {
        // Given
        String emptyLang = "";
        String emptyAuthor = "";
        when(useCase.findAuthorMetadata(emptyAuthor, emptyLang)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AuthorDto> result = controller.refreshAuthor(emptyLang, emptyAuthor);

        // Then
        verify(useCase, times(1)).findAuthorMetadata(emptyAuthor, emptyLang);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void refreshAuthor_WhenUseCaseThrowsException_ShouldPropagateException() {
        // Given
        when(useCase.findAuthorMetadata(anyString(), anyString()))
            .thenThrow(new RuntimeException("Test exception"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.refreshAuthor(lang, author);
        });

        verify(useCase, times(1)).findAuthorMetadata(author, lang);
        verify(mapper, never()).domain2Dto(any());
    }

    @Test
    void refreshAuthor_WhenMapperThrowsException_ShouldPropagateException() {
        // Given
        when(useCase.findAuthorMetadata(author, lang)).thenReturn(Optional.of(domainAuthor));
        when(mapper.domain2Dto(domainAuthor)).thenThrow(new RuntimeException("Mapping exception"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.refreshAuthor(lang, author);
        });

        verify(useCase, times(1)).findAuthorMetadata(author, lang);
        verify(mapper, times(1)).domain2Dto(domainAuthor);
    }

    @Test
    void refreshAuthor_ShouldAlwaysReturnHttpStatusOk() {
        // Given
        when(useCase.findAuthorMetadata(anyString(), anyString())).thenReturn(Optional.empty());

        // When
        ResponseEntity<AuthorDto> result = controller.refreshAuthor(lang, author);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(200, result.getStatusCodeValue());
    }
}