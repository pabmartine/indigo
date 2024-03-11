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

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class FindAllAuthorsControllerIntegrationTest extends BaseIndigoIntegrationTest {

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
	public void findAllAuthorsWithMultipleLanguages() throws Exception {

		//Given

		//When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/author/all")
				.param("languages", "eng", "spa")
				.param("page", "0")
				.param("size", "10")
				.param("sort", "name")
				.param("order", "asc")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		//Then
		result.andExpect(jsonPath("$.[0].name", Matchers.is(authorMongoEntity.getName())));
		result.andExpect(jsonPath("$.[0].description", Matchers.is(authorMongoEntity.getDescription())));
		result.andExpect(jsonPath("$.[0].numBooks", Matchers.is(authorMongoEntity.getNumBooks().getTotal())));

		result.andExpect(jsonPath("$.[1].name", Matchers.is(authorMongoEntity2.getName())));
		result.andExpect(jsonPath("$.[1].description", Matchers.is(authorMongoEntity2.getDescription())));
		result.andExpect(jsonPath("$.[1].numBooks", Matchers.is(authorMongoEntity2.getNumBooks().getTotal())));
	}

	@Test
	@WithMockUser
	public void findAllAuthorsLimit1() throws Exception {

		//Given

		//When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/author/all")
				.param("languages", "eng", "spa")
				.param("page", "0")
				.param("size", "1")
				.param("sort", "name")
				.param("order", "asc")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		//Then
		result.andExpect(jsonPath("$.[0].name", Matchers.is(authorMongoEntity.getName())));
		result.andExpect(jsonPath("$.[0].description", Matchers.is(authorMongoEntity.getDescription())));
		result.andExpect(jsonPath("$.[0].numBooks", Matchers.is(authorMongoEntity.getNumBooks().getTotal())));
	}

	@Test
	@WithMockUser
	public void findAllAuthorsSortDesc() throws Exception {

		//Given

		//When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/author/all")
				.param("languages", "eng", "spa")
				.param("page", "0")
				.param("size", "10")
				.param("sort", "name")
				.param("order", "asc")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		//Then
		result.andExpect(jsonPath("$.[0].name", Matchers.is(authorMongoEntity.getName())));
		result.andExpect(jsonPath("$.[0].description", Matchers.is(authorMongoEntity.getDescription())));
		result.andExpect(jsonPath("$.[0].numBooks", Matchers.is(authorMongoEntity.getNumBooks().getTotal())));
	}

	@Test
	@WithMockUser
	public void findAllAuthorsSingleLanguage() throws Exception {

		//Given

		//When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/author/all")
				.param("languages", "eng")
				.param("page", "0")
				.param("size", "10")
				.param("sort", "name")
				.param("order", "asc")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		//Then
		result.andExpect(jsonPath("$.[0].name", Matchers.is(authorMongoEntity.getName())));
		result.andExpect(jsonPath("$.[0].description", Matchers.is(authorMongoEntity.getDescription())));
		result.andExpect(jsonPath("$.[0].numBooks", Matchers.is(2)));

		result.andExpect(jsonPath("$.[1].name", Matchers.is(authorMongoEntity2.getName())));
		result.andExpect(jsonPath("$.[1].description", Matchers.is(authorMongoEntity2.getDescription())));
		result.andExpect(jsonPath("$.[1].numBooks", Matchers.is(1)));
	}

	@Test
	@WithMockUser
	public void findAllAuthorsSingleNoLanguageCoincidence() throws Exception {

		//Given

		//When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/author/all")
				.param("languages", "fra")
				.param("page", "0")
				.param("size", "10")
				.param("sort", "name")
				.param("order", "asc")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		//Then
		assertEquals("[]", result.andReturn().getResponse().getContentAsString());
	}
}
