package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshBookMetadataUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshBookMetadataControllerUnitTest {

    @Mock
    private RefreshBookMetadataUseCase useCase;

    @Mock
    private BookDtoMapper mapper;

    @InjectMocks
    private RefreshBookMetadataController controller;

    private String book;
    private String lang;
    private Book domainBook;
    private BookDto bookDto;

    @BeforeEach
    void setUp() {
        book = "test-book";
        lang = "en";

        domainBook = new Book();
        domainBook.setId("book123");
        domainBook.setTitle("Test Book");

        bookDto = new BookDto();
        bookDto.setId("book123");
        bookDto.setTitle("Test Book");
    }

    @Test
    void refreshBook_WithValidParameters_ShouldReturnBookDto() {
        // Given
        when(useCase.findBookMetadata(book, lang)).thenReturn(Optional.of(domainBook));
        when(mapper.domain2Dto(domainBook)).thenReturn(bookDto);

        // When
        ResponseEntity<BookDto> result = controller.refreshBook(book, lang);

        // Then
        verify(useCase, times(1)).findBookMetadata(book, lang);
        verify(mapper, times(1)).domain2Dto(domainBook);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(bookDto, result.getBody());
    }

    @Test
    void refreshBook_WhenBookNotFound_ShouldReturnNullBody() {
        // Given
        when(useCase.findBookMetadata(book, lang)).thenReturn(Optional.empty());

        // When
        ResponseEntity<BookDto> result = controller.refreshBook(book, lang);

        // Then
        verify(useCase, times(1)).findBookMetadata(book, lang);
        verify(mapper, never()).domain2Dto(any());
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void refreshBook_WithDifferentLanguage_ShouldCallUseCaseWithCorrectParameters() {
        // Given
        String spanishLang = "es";
        when(useCase.findBookMetadata(book, spanishLang)).thenReturn(Optional.of(domainBook));
        when(mapper.domain2Dto(domainBook)).thenReturn(bookDto);

        // When
        ResponseEntity<BookDto> result = controller.refreshBook(book, spanishLang);

        // Then
        verify(useCase, times(1)).findBookMetadata(book, spanishLang);
        verify(mapper, times(1)).domain2Dto(domainBook);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(bookDto, result.getBody());
    }

    @Test
    void refreshBook_WithDifferentBook_ShouldCallUseCaseWithCorrectParameters() {
        // Given
        String differentBook = "different-book";
        when(useCase.findBookMetadata(differentBook, lang)).thenReturn(Optional.of(domainBook));
        when(mapper.domain2Dto(domainBook)).thenReturn(bookDto);

        // When
        ResponseEntity<BookDto> result = controller.refreshBook(differentBook, lang);

        // Then
        verify(useCase, times(1)).findBookMetadata(differentBook, lang);
        verify(mapper, times(1)).domain2Dto(domainBook);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(bookDto, result.getBody());
    }

    @Test
    void refreshBook_WhenUseCaseThrowsException_ShouldPropagateException() {
        // Given
        when(useCase.findBookMetadata(anyString(), anyString()))
            .thenThrow(new RuntimeException("Test exception"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.refreshBook(book, lang);
        });

        verify(useCase, times(1)).findBookMetadata(book, lang);
        verify(mapper, never()).domain2Dto(any());
    }

    @Test
    void refreshBook_WhenMapperThrowsException_ShouldPropagateException() {
        // Given
        when(useCase.findBookMetadata(book, lang)).thenReturn(Optional.of(domainBook));
        when(mapper.domain2Dto(domainBook)).thenThrow(new RuntimeException("Mapping exception"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            controller.refreshBook(book, lang);
        });

        verify(useCase, times(1)).findBookMetadata(book, lang);
        verify(mapper, times(1)).domain2Dto(domainBook);
    }

    @Test
    void refreshBook_ShouldAlwaysReturnHttpStatusOk() {
        // Given
        when(useCase.findBookMetadata(anyString(), anyString())).thenReturn(Optional.empty());

        // When
        ResponseEntity<BookDto> result = controller.refreshBook(book, lang);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(200, result.getStatusCodeValue());
    }
}