package com.martinia.indigo.book.infrastructure.api.similar;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

class FindSimilarBooksControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private BookMongoEntity bookMongoEntity;

	private BookMongoEntity bookMongoEntity2;

	@BeforeEach
	public void init() {
		bookMongoEntity = BookMongoEntity.builder()
				.id("64dce11b1520b348ff4b96ae")
				.title("title")
				.languages(List.of("spa"))
				.similar(Arrays.asList("similar"))
				.build();
		bookRepository.save(bookMongoEntity);

		bookMongoEntity2 = BookMongoEntity.builder().id("64dcec021520b348ff4be9d6").title("title2").languages(List.of("eng")).build();
		bookRepository.save(bookMongoEntity2);

	}

	@Test
	void testGetSimilar() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/similar")
				.param("similar", "64dce11b1520b348ff4b96ae")
				.param("languages", "spa", "eng"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		List<BookDto> list = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(),
				new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());

	}

	@Test
	void testGetSimilarNotExist() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/similar")
				.param("similar", "00dce11b1520b348ff4b96ae")
				.param("languages", "spa", "eng"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		List<BookDto> list = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(),
				new TypeReference<List<BookDto>>() {});
		assertEquals(Collections.emptyList(), list);

	}

	@Test
	void testGetSimilarNoLanguage() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/similar").param("similar", "64dce11b1520b348ff4b96ae").param("languages", "fra"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		List<BookDto> list = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(),
				new TypeReference<List<BookDto>>() {});
		assertEquals(Collections.emptyList(), list);

	}

}