package com.martinia.indigo.author.infrastructure.api.controllers.favorite;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.favorite.DeleteFavoriteAuthorUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeleteFavoriteAuthorControllerIntegrationTest extends BaseIndigoIntegrationTest {



	@Resource
	private MockMvc mockMvc;

	private UserMongoEntity userMongoEntity;

	@BeforeEach
	public void init() {
		final Map<String, Integer> spaEngMap = new HashMap<>();
		spaEngMap.put("spa", 1);
		spaEngMap.put("eng", 2);

		userMongoEntity = UserMongoEntity.builder().username("test").favoriteAuthors(Arrays.asList("author1", "author2")).build();
		userRepository.save(userMongoEntity);
	}

	@Test
	@WithMockUser
	public void deleteFavoriteAuthor() throws Exception {
		// Given


		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/author/favorite").param("author", userMongoEntity.getFavoriteAuthors().get(0)).param("user", userMongoEntity.getUsername())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		UserMongoEntity user = userRepository.findByUsername(userMongoEntity.getUsername()).get();
		assertEquals(1, user.getFavoriteAuthors().size());
		assertEquals(userMongoEntity.getFavoriteAuthors().get(1), user.getFavoriteAuthors().get(0));

	}

	@Test
	@WithMockUser
	public void deleteFavoriteAuthorNonExistingAuthor() throws Exception {
		// Given


		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/author/favorite").param("author", "unknown").param("user", userMongoEntity.getUsername())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		UserMongoEntity user = userRepository.findByUsername(userMongoEntity.getUsername()).get();
		assertEquals(2, user.getFavoriteAuthors().size());
		assertEquals(userMongoEntity.getFavoriteAuthors().get(0), user.getFavoriteAuthors().get(0));
		assertEquals(userMongoEntity.getFavoriteAuthors().get(1), user.getFavoriteAuthors().get(1));

	}

	@Test
	@WithMockUser
	public void deleteFavoriteAuthorNonExistingUser() throws Exception {
		// Given


		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/author/favorite").param("author", userMongoEntity.getFavoriteAuthors().get(0)).param("user", "unknown")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		UserMongoEntity user = userRepository.findByUsername(userMongoEntity.getUsername()).get();
		assertEquals(2, user.getFavoriteAuthors().size());
		assertEquals(userMongoEntity.getFavoriteAuthors().get(0), user.getFavoriteAuthors().get(0));
		assertEquals(userMongoEntity.getFavoriteAuthors().get(1), user.getFavoriteAuthors().get(1));
	}
}
