package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.DeleteBookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeleteBookControllerTest extends BaseIndigoTest {

	@MockBean
	private DeleteBookUseCase deleteBookUseCase;

	@Resource
	private DeleteBookController deleteBookController;
	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testDeleteBook() throws Exception {
		// Given
		String bookId = "book123";

		// When
		ResultActions resultActions = mockMvc.perform(
				delete("/api/book/delete").param("id", bookId).contentType(MediaType.APPLICATION_JSON));

		// Then
		// Verificar que el estado de la respuesta sea 200 OK
		resultActions.andExpect(status().isOk());
		// Verificar que el método delete del useCase es llamado con el ID del libro
		verify(deleteBookUseCase).delete(bookId);
	}

}
