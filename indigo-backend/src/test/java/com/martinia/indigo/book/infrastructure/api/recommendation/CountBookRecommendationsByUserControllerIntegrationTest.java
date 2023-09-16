package com.martinia.indigo.book.infrastructure.api.recommendation;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.UUID;

class CountBookRecommendationsByUserControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	private UserMongoEntity userMongoEntity;

	private BookMongoEntity bookMongoEntity;

	private BookMongoEntity bookMongoEntity2;

	private BookMongoEntity bookMongoEntity3;

	@BeforeEach
	public void init() {
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
	void testCountBookRecommendationsByUserWithNoRecommendations() throws Exception {
		// Given
		NotificationMongoEntity notificationMongoEntity = NotificationMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.user(userMongoEntity.getUsername())
				.book(bookMongoEntity.getPath())
				.build();
		notificationRepository.save(notificationMongoEntity);

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/recommendations/user/count").param("user", userMongoEntity.getUsername()));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
	}

	@Test
	void testCountBookRecommendationsByUserOk() throws Exception {
		// Given
		NotificationMongoEntity notificationMongoEntity = NotificationMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.user(userMongoEntity.getUsername())
				.book(bookMongoEntity3.getPath())
				.build();
		notificationRepository.save(notificationMongoEntity);

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/recommendations/user/count").param("user", userMongoEntity.getUsername()));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
	}

}