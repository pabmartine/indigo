package com.martinia.indigo.author.infrastructure.api.controllers.favorite;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddFavoriteAuthorControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	private UserMongoEntity userMongoEntity;

	@BeforeEach
	public void init() {
		final Map<String, Integer> spaEngMap = new HashMap<>();
		spaEngMap.put("spa", 1);
		spaEngMap.put("eng", 2);

		userMongoEntity = UserMongoEntity.builder().username("test").build();
		userRepository.save(userMongoEntity);
	}

	@Test
	@WithMockUser
	public void addFavoriteAuthorTest() throws Exception {
		// Given
		final String author = "author1";

		// When
		mockMvc.perform(MockMvcRequestBuilders.post("/api/author/favorite")
				.param("author", author)
				.param("user", userMongoEntity.getUsername())
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		UserMongoEntity user = userRepository.findByUsername(userMongoEntity.getUsername()).get();
		assertEquals(1, user.getFavoriteAuthors().size());
		assertEquals(author, user.getFavoriteAuthors().get(0));
	}

	@Test
	@WithMockUser
	public void addFavoriteAuthorNonExistingUserTest() throws Exception {
		// Given
		final String author = "author1";

		// When
		mockMvc.perform(MockMvcRequestBuilders.post("/api/author/favorite")
				.param("author", author)
				.param("user", "user2")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		assertEquals(1, userRepository.count());
		assertEquals(0, userRepository.findByUsername(userMongoEntity.getUsername()).get().getFavoriteAuthors().stream().count());

	}

}