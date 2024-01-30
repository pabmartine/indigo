package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindFavoriteBooksControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private BookMongoEntity bookMongoEntity;

	private BookMongoEntity bookMongoEntity2;

	private UserMongoEntity userMongoEntity;

	private UserMongoEntity userMongoEntity2;

	@BeforeEach
	public void init() {

		bookMongoEntity = BookMongoEntity.builder().id(UUID.randomUUID().toString()).title("title").path("path").build();
		bookRepository.save(bookMongoEntity);

		bookMongoEntity2 = BookMongoEntity.builder().id(UUID.randomUUID().toString()).title("title2").path("path2").build();
		bookRepository.save(bookMongoEntity2);

		userMongoEntity = UserMongoEntity.builder().username("test").favoriteBooks(Arrays.asList(bookMongoEntity.getPath())).build();
		userRepository.save(userMongoEntity);

		userMongoEntity2 = UserMongoEntity.builder().username("test2").build();
		userRepository.save(userMongoEntity2);
	}

	@Test
	@WithMockUser
	public void findFavoriteAuthors() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(
						MockMvcRequestBuilders.get("/api/book/favorites").param("user", "test").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value(bookMongoEntity.getTitle()))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].path").value(bookMongoEntity.getPath()));
	}

	@Test
	@WithMockUser
	public void findFavoriteAuthorsWithNoFavorites() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(
						MockMvcRequestBuilders.get("/api/book/favorites").param("user", "test2").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		assertEquals("[]", result.andReturn().getResponse().getContentAsString());
	}

	@Test
	@WithMockUser
	public void findFavoriteAuthorsWithUnknownUser() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(
						MockMvcRequestBuilders.get("/api/book/favorites").param("user", "unknown").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());
		;

		// Then
		assertEquals("[]", result.andReturn().getResponse().getContentAsString());

	}

}