package com.martinia.indigo.book.infrastructure.api.serie;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

class FindBooksBySerieControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private BookMongoEntity bookMongoEntity;

	private BookMongoEntity bookMongoEntity2;

	final String SERIE = "serie";

	@BeforeEach
	public void init() {
		bookMongoEntity = BookMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.title("title")
				.languages(List.of("spa"))
				.serie(SerieMongo.builder().index(1).name(SERIE).build())
				.build();
		bookRepository.save(bookMongoEntity);

		bookMongoEntity2 = BookMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.title("title2")
				.languages(List.of("eng"))
				.serie(SerieMongo.builder().index(2).name(SERIE).build())
				.build();
		bookRepository.save(bookMongoEntity2);
	}

	@Test
	void testGetSerie() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/serie").param("serie", SERIE).param("languages", "spa"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		List<BookDto> list = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(),
				new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());

	}

	@Test
	void testGetSerieNoExist() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/serie").param("serie", "unknown").param("languages", "spa"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		List<BookDto> list = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(),
				new TypeReference<List<BookDto>>() {});
		assertEquals(Collections.emptyList(), list);

	}

	@Test
	void testGetSerieNoLanguages() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/serie").param("serie", SERIE).param("languages", "fra"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		List<BookDto> list = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(),
				new TypeReference<List<BookDto>>() {});
		assertEquals(Collections.emptyList(), list);
	}
}