package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeleteBookControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	private BookMongoEntity bookMongoEntity;

	@BeforeEach
	public void init() {
		bookMongoEntity = BookMongoEntity.builder().id(UUID.randomUUID().toString()).title("title").build();
		bookRepository.save(bookMongoEntity);
	}

	@Test
	@WithMockUser
	public void testDeleteBook() throws Exception {
		// Given

		// When
		ResultActions resultActions = mockMvc.perform(
				delete("/api/book/delete").param("id", bookMongoEntity.getId()).contentType(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk());

		assertTrue(bookRepository.findById(bookMongoEntity.getId()).isEmpty());
	}

	@Test
	@WithMockUser
	public void testDeleteBookNotExist() throws Exception {
		// Given
		final String bookId = UUID.randomUUID().toString();

		// When
		ResultActions resultActions = mockMvc.perform(
				delete("/api/book/delete").param("id", bookId).contentType(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk());
		assertTrue(bookRepository.findById(bookMongoEntity.getId()).isPresent());
	}

}
