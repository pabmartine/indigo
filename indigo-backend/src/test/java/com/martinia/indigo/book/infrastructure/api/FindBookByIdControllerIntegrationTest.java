package com.martinia.indigo.book.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class FindBookByIdControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private BookMongoEntity bookMongoEntity;

	private BookMongoEntity bookMongoEntity2;

	@BeforeEach
	@SneakyThrows
	public void init() {
		bookMongoEntity = BookMongoEntity.builder()
				.id("64dce11b1520b348ff4b96ae")
				.title("title")
				.path("path")
				.languages(List.of("spa"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.pages(100)
				.tags(Arrays.asList("tag"))
				.build();
		bookRepository.save(bookMongoEntity);

		bookMongoEntity2 = BookMongoEntity.builder().id("64dcec021520b348ff4be9d6").title("title2").languages(List.of("spa")).build();
		bookRepository.save(bookMongoEntity2);

	}

	@Test
	void testGetBookById() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/id").param("id", bookMongoEntity.getId()));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(jsonPath("$.id", Matchers.is(bookMongoEntity.getId())));

	}

	@Test
	void testGetBookByIdNotFound() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/id").param("id", "unknown"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		assertEquals("", result.andReturn().getResponse().getContentAsString());

	}
}