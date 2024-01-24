package com.martinia.indigo.book.infrastructure.api.recommendation;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

class FindBookRecommendationsByBookControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private UserMongoEntity userMongoEntity;

	private BookMongoEntity bookMongoEntity;

	private BookMongoEntity bookMongoEntity2;

	private BookMongoEntity bookMongoEntity3;

	@BeforeEach
	public void init() {

		configurationRepository.deleteAll();
		configurationRepository.save(ConfigurationMongoEntity.builder().key("books.recommendations").value("10").build());

		userMongoEntity = UserMongoEntity.builder()
				.id("64dcd64d1520b348ff4b31ec")
				.username("user")
				.languageBooks(Arrays.asList("spa", "eng"))
				.build();
		userRepository.save(userMongoEntity);

		bookMongoEntity = BookMongoEntity.builder()
				.id("64dcdb231520b348ff4b6139")
				.title("title1")
				.languages(Arrays.asList("spa"))
				.path("path1")
				.build();
		bookRepository.save(bookMongoEntity);

		bookMongoEntity2 = BookMongoEntity.builder()
				.id("64dce11b1520b348ff4b96ae")
				.title("title2")
				.languages(Arrays.asList("fra"))
				.path("path2")
				.build();
		bookRepository.save(bookMongoEntity2);

		bookMongoEntity3 = BookMongoEntity.builder()
				.id("64dcec021520b348ff4be9d6")
				.title("title3")
				.recommendations(Arrays.asList(bookMongoEntity.getId(), bookMongoEntity2.getId()))
				.languages(Arrays.asList("eng"))
				.path("path3")
				.build();
		bookRepository.save(bookMongoEntity3);

	}

	@Test
	void testFindBookRecommendationsByBookWithNoLanguages() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/recommendations/book")
				.param("recommendations", "64dcd64d1520b348ff4b31ec", "64dce11b1520b348ff4b96ae", "64dcec021520b348ff4be9d6")
				.param("languages", "gal"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());

	}

	@Test
	void testFindBookRecommendationsByBookWithNoRecommendations() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/recommendations/book")
				.param("recommendations", "000cdb231520b348ff4b6139")
				.param("languages", "eng", "spa", "fra"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());

	}

	@Test
	void testFindBookRecommendationsByBookOk() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/recommendations/book")
				.param("recommendations", "64dcd64d1520b348ff4b31ec", "64dcdb231520b348ff4b6139")
				.param("languages", "eng", "spa", "fra"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists());

	}
}