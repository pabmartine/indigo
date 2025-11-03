package com.martinia.indigo.author.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class CountAllAuthorsControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private AuthorMongoEntity authorMongoEntity;

	private AuthorMongoEntity authorMongoEntity2;

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
	}

	@Test
	@WithMockUser
	public void countAllAuthors_ShouldReturnTotalCount() throws Exception {

		//Given
		long expectedCount = 2L; // Assuming 2 authors are saved in init()

		//When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/count")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());

		//Then
		assertEquals(String.valueOf(expectedCount), result.andReturn().getResponse().getContentAsString());
	}
}