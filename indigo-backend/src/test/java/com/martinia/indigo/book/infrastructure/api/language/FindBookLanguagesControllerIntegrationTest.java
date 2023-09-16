package com.martinia.indigo.book.infrastructure.api.language;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

class FindBookLanguagesControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

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
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("fra"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]").value("eng"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[2]").value("spa"));
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