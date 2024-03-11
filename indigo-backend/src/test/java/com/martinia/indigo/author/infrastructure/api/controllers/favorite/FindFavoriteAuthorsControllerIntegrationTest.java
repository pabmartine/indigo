package com.martinia.indigo.author.infrastructure.api.controllers.favorite;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FindFavoriteAuthorsControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private AuthorMongoEntity authorMongoEntity;

	private AuthorMongoEntity authorMongoEntity2;

	private UserMongoEntity userMongoEntity;

	private UserMongoEntity userMongoEntity2;

	@BeforeEach
	public void init() {
		final Map<String, Integer> spaEngMap = new HashMap<>();
		spaEngMap.put("spa", 1);
		spaEngMap.put("eng", 2);

		authorMongoEntity = AuthorMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.name("name")
				.sort("sort")
				.description("description")
				.numBooks(NumBooksMongo.builder().total(3).languages(spaEngMap).build())
				.build();
		authorRepository.save(authorMongoEntity);

		final Map<String, Integer> engMap = new HashMap<>();
		engMap.put("eng", 1);

		authorMongoEntity2 = AuthorMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.name("name2")
				.sort("sort2")
				.description("description2")
				.numBooks(NumBooksMongo.builder().total(1).languages(engMap).build())
				.build();
		authorRepository.save(authorMongoEntity2);

		userMongoEntity = UserMongoEntity.builder().username("test").favoriteAuthors(Arrays.asList(authorMongoEntity.getSort())).build();
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
						MockMvcRequestBuilders.get("/api/author/favorites").param("user", "test").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(authorMongoEntity.getName()))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].sort").value(authorMongoEntity.getSort()))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(authorMongoEntity.getDescription()))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].numBooks").value(authorMongoEntity.getNumBooks().getTotal()));
	}

	@Test
	@WithMockUser
	public void findFavoriteAuthorsWithNoFavorites() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(
						MockMvcRequestBuilders.get("/api/author/favorites").param("user", "test2").contentType(MediaType.APPLICATION_JSON))
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
						MockMvcRequestBuilders.get("/api/author/favorites").param("user", "unknown").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());
		;

		// Then
		assertEquals("[]", result.andReturn().getResponse().getContentAsString());

	}
}
