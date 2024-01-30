package com.martinia.indigo.book.infrastructure.api.sent;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.is;

class FindSentBooksControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private UserMongoEntity userMongoEntity;

	private BookMongoEntity bookMongoEntity;

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

		NotificationMongoEntity notificationMongoEntity = NotificationMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.user(userMongoEntity.getUsername())
				.book(bookMongoEntity.getPath())
				.build();
		notificationRepository.save(notificationMongoEntity);
	}

	@Test
	void testGetSentBooksOk() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/sent").param("user", userMongoEntity.getUsername()));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", is(bookMongoEntity.getId())));

	}

	@Test
	void testGetSentBooksNoUser() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/sent").param("user", "unknown"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());

	}

}