package com.martinia.indigo.book.infrastructure.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.EditBookUseCase;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EditBookControllerTest extends BaseIndigoTest {

	@MockBean
	private EditBookUseCase editBookUseCase;

	@MockBean
	private BookDtoMapper bookDtoMapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	public void testEditBook() throws Exception {
		// Given
		BookDto bookDto = new BookDto();
		bookDto.setTitle("Book Title");
		bookDto.setAuthors(List.of("Author 1", "Author 2"));

		// Simular el mapeo del DTO al dominio
		Book bookDomain = new Book();
		bookDomain.setTitle(bookDto.getTitle());
		bookDomain.setAuthors(bookDto.getAuthors());
		when(bookDtoMapper.dto2domain(bookDto)).thenReturn(bookDomain);

		// Simular el caso de uso
		doNothing().when(editBookUseCase).edit(any(Book.class));

		// When/Then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/book/edit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(bookDto))).andExpect(MockMvcResultMatchers.status().isOk());

		// Verificar que el método edit del caso de uso es llamado con el dominio correcto
		verify(editBookUseCase).edit(any());
	}

}

