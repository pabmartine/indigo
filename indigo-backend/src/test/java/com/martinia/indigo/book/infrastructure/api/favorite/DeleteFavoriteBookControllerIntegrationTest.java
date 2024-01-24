package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteFavoriteBookControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private UserMongoEntity userMongoEntity;

	@BeforeEach
	public void init() {

		userMongoEntity = UserMongoEntity.builder().username("test").favoriteBooks(Arrays.asList("book1", "book2")).build();
		userRepository.save(userMongoEntity);
	}

	@Test
	@WithMockUser
	public void deleteFavoriteAuthor() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/book/favorite")
				.param("book", userMongoEntity.getFavoriteBooks().get(0))
				.param("user", userMongoEntity.getUsername())
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		UserMongoEntity user = userRepository.findByUsername(userMongoEntity.getUsername()).get();
		assertEquals(1, user.getFavoriteBooks().size());
		assertEquals(userMongoEntity.getFavoriteBooks().get(1), user.getFavoriteBooks().get(0));

	}

	@Test
	@WithMockUser
	public void deleteFavoriteAuthorNonExistingAuthor() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/book/favorite")
				.param("book", "unknown")
				.param("user", userMongoEntity.getUsername())
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		UserMongoEntity user = userRepository.findByUsername(userMongoEntity.getUsername()).get();
		assertEquals(2, user.getFavoriteBooks().size());
		assertEquals(userMongoEntity.getFavoriteBooks().get(0), user.getFavoriteBooks().get(0));
		assertEquals(userMongoEntity.getFavoriteBooks().get(1), user.getFavoriteBooks().get(1));

	}

	@Test
	@WithMockUser
	public void deleteFavoriteAuthorNonExistingUser() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/book/favorite")
				.param("book", userMongoEntity.getFavoriteBooks().get(0))
				.param("user", "unknown")
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		UserMongoEntity user = userRepository.findByUsername(userMongoEntity.getUsername()).get();
		assertEquals(2, user.getFavoriteBooks().size());
		assertEquals(userMongoEntity.getFavoriteBooks().get(0), user.getFavoriteBooks().get(0));
		assertEquals(userMongoEntity.getFavoriteBooks().get(1), user.getFavoriteBooks().get(1));
	}

}