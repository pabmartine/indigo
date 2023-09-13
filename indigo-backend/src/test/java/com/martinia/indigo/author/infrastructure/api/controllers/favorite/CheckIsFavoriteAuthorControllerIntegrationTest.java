package com.martinia.indigo.author.infrastructure.api.controllers.favorite;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckIsFavoriteAuthorControllerIntegrationTest extends BaseIndigoIntegrationTest {

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
	public void checkIsFavoriteAuthorOk() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/author/favorite")
				.param("author", userMongoEntity.getFavoriteAuthors().get(0))
				.param("user", userMongoEntity.getUsername())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		assertTrue(Boolean.valueOf(result.andReturn().getResponse().getContentAsString()));
	}

	@Test
	@WithMockUser
	public void checkIsFavoriteAuthorAuthorNotExist() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/author/favorite")
				.param("author", "unknown")
				.param("user", userMongoEntity.getUsername())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		assertFalse(Boolean.valueOf(result.andReturn().getResponse().getContentAsString()));

	}

	@Test
	@WithMockUser
	public void checkIsFavoriteAuthorUserNotExist() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/author/favorite")
				.param("author", userMongoEntity.getFavoriteAuthors().get(0))
				.param("user", "unknown")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		assertFalse(Boolean.valueOf(result.andReturn().getResponse().getContentAsString()));
	}
}

