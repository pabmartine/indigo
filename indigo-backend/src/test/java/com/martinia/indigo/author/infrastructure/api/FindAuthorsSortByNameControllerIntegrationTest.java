package com.martinia.indigo.author.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class FindAuthorsSortByNameControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	private AuthorMongoEntity authorMongoEntity;

	@BeforeEach
	public void init() {
		authorMongoEntity = AuthorMongoEntity.builder()
				.id(UUID.randomUUID().toString())
				.name("name")
				.sort("sort")
				.description("description")
				.build();
		authorRepository.save(authorMongoEntity);
	}

	@Test
	@WithMockUser
	public void findAuthorsSortByName() throws Exception {

		//Given
		final String sort = "sort";

		//When
		ResultActions result = mockMvc.perform(
						MockMvcRequestBuilders.get("/api/author/all")
								.param("languages", "English", "Spanish").param("page", "1")
								.param("size", "10").param("sort", "name").param("order", "asc").contentType(MediaType.APPLICATION_JSON))
								.andExpect(MockMvcResultMatchers.status().isOk());

		//Then
		result.andExpect(jsonPath("$.name", Matchers.is(authorMongoEntity.getName())));
		result.andExpect(jsonPath("$.sort", Matchers.is(authorMongoEntity.getSort())));
		result.andExpect(jsonPath("$.description", Matchers.is(authorMongoEntity.getDescription())));
		result.andExpect(jsonPath("$.provider", Matchers.is(authorMongoEntity.getProvider())));
		result.andExpect(jsonPath("$.image", Matchers.is(authorMongoEntity.getImage())));
		result.andExpect(jsonPath("$.numBooks", Matchers.is(0)));
	}


}
