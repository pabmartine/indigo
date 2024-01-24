package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckIsFavoriteBookControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private UserMongoEntity userMongoEntity;

	@BeforeEach
	public void init() {
		userMongoEntity = UserMongoEntity.builder().username("test").favoriteBooks(Arrays.asList("book1", "book2")).build();
		userRepository.save(userMongoEntity);
	}

	@Test
	@WithMockUser
	public void checkIsFavoriteBookOk() throws Exception {

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/favorite")
				.param("user", userMongoEntity.getUsername())
				.param("book", userMongoEntity.getFavoriteBooks().get(0)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		assertTrue(Boolean.valueOf(result.andReturn().getResponse().getContentAsString()));
	}

	@Test
	@WithMockUser
	public void checkIsFavoriteBookNotExist() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/favorite").param("user", userMongoEntity.getUsername()).param("book", "unknown"));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		assertFalse(Boolean.valueOf(result.andReturn().getResponse().getContentAsString()));

	}

	@Test
	@WithMockUser
	public void checkIsFavoriteBookUserNotExist() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/favorite")
				.param("user", "unknown")
				.param("book", userMongoEntity.getFavoriteBooks().get(0)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		assertFalse(Boolean.valueOf(result.andReturn().getResponse().getContentAsString()));
	}

}