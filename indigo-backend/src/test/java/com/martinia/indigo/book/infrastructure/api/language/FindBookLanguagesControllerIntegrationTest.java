package com.martinia.indigo.book.infrastructure.api.language;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

class FindBookLanguagesControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Test
	void testGetBookLanguages() throws Exception {
		// Given
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.title("title")
				.path("path")
				.languages(Arrays.asList("spa", "eng"))
				.build();
		bookRepository.save(bookMongoEntity);

		BookMongoEntity bookMongoEntity2 = BookMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.title("title2")
				.languages(Arrays.asList("spa", "fra"))
				.path("path2")
				.build();
		bookRepository.save(bookMongoEntity2);

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/languages"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", org.hamcrest.Matchers.containsInAnyOrder("fra", "eng", "spa")));
	}

	@Test
	void regionalLanguagesAreSavedAndListedOnlyOnce() throws Exception {
		var book = bookRepository.save(BookMongoEntity.builder()
				.languages(Arrays.asList("es-ES", "es_AR", "en-EN")).build());
		assertEquals(Arrays.asList("es", "en"), bookRepository.findById(book.getId()).orElseThrow().getLanguages());
		mockMvc.perform(MockMvcRequestBuilders.get("/api/book/languages"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", org.hamcrest.Matchers.containsInAnyOrder("es", "en")));
	}

	@Test
	void testGetBookNoLanguages() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/languages"));

		// Then
		assertEquals("[]", result.andReturn().getResponse().getContentAsString());
	}
}
