package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddFavoriteBookControllerIntegrationTest extends BaseIndigoIntegrationTest {

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
	void addFavoriteBookTest() throws Exception {
		// Given
		final String book = "book1";

		// When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/book/favorite").param("user", userMongoEntity.getUsername()).param("book", book));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		UserMongoEntity user = userRepository.findByUsername(userMongoEntity.getUsername()).get();
		assertEquals(1, user.getFavoriteBooks().size());
		assertEquals(book, user.getFavoriteBooks().get(0));
	}

	@Test
	@WithMockUser
	void addFavoriteBookNonExistingUserTest() throws Exception {
		// Given
		final String book = "book1";

		// When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/book/favorite").param("user", "unknown").param("book", book));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		assertEquals(1, userRepository.count());
		assertEquals(0, userRepository.findByUsername(userMongoEntity.getUsername()).get().getFavoriteBooks().stream().count());
	}

}