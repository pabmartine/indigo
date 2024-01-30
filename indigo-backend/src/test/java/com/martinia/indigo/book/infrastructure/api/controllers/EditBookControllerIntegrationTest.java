package com.martinia.indigo.book.infrastructure.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EditBookControllerIntegrationTest extends BaseIndigoTest {

	@Resource
	private MockMvc mockMvc;

	private BookMongoEntity bookMongoEntity;

	@BeforeEach
	public void init() {
		bookMongoEntity = BookMongoEntity.builder().id(UUID.randomUUID().toString()).title("title").image("/9image").build();
		bookRepository.save(bookMongoEntity);
	}

	@Test
	@WithMockUser
	public void testEditBook() throws Exception {
		// Given
		final BookDto bookDto = BookDto.builder().id(bookMongoEntity.getId()).image("data:/9image").title("title2").build();

		// When
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/api/book/edit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(bookDto)));

		//Then
		resultActions.andExpect(status().isOk());
		assertTrue(bookRepository.findById(bookMongoEntity.getId()).isPresent());
		assertEquals(bookDto.getTitle(), bookRepository.findById(bookMongoEntity.getId()).get().getTitle());
	}

	@Test
	@WithMockUser
	public void testEditBookWithNullImageInTarget() throws Exception {
		// Given
		final BookDto bookDto = BookDto.builder().id(bookMongoEntity.getId()).image("null").title("title2").build();

		// When
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/api/book/edit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(bookDto)));

		//Then
		resultActions.andExpect(status().isOk());
		assertTrue(bookRepository.findById(bookMongoEntity.getId()).isPresent());
		final BookMongoEntity book = bookRepository.findById(bookMongoEntity.getId()).get();
		assertEquals(bookDto.getTitle(), book.getTitle());
		assertEquals(bookMongoEntity.getImage(), book.getImage());
	}

	@Test
	@WithMockUser
	public void testEditBookWithNullImageInSource() throws Exception {
		// Given
		bookMongoEntity.setImage(null);
		bookRepository.save(bookMongoEntity);

		final BookDto bookDto = BookDto.builder().id(bookMongoEntity.getId()).image("null").title("title2").build();

		// When
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/api/book/edit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(bookDto)));

		//Then
		resultActions.andExpect(status().isOk());
		assertTrue(bookRepository.findById(bookMongoEntity.getId()).isPresent());
		final BookMongoEntity book = bookRepository.findById(bookMongoEntity.getId()).get();
		assertEquals(bookDto.getTitle(), book.getTitle());
		assertNull(book.getImage());
	}

	@Test
	@WithMockUser
	public void testEditBookNotExist() throws Exception {
		// Given
		final BookDto bookDto = BookDto.builder().id(UUID.randomUUID().toString()).title("title2").build();

		// When
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/api/book/edit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(bookDto)));

		//Then
		resultActions.andExpect(status().isOk());
		assertTrue(bookRepository.findById(bookMongoEntity.getId()).isPresent());
		assertEquals(bookMongoEntity.getTitle(), bookRepository.findById(bookMongoEntity.getId()).get().getTitle());

	}

}

