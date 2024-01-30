package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.domain.model.RolesEnum;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindUserByIdControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Test
	public void testFindByIdNotExit() throws Exception {
		// Given
		String id = "1";

		// When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/user/getById").param("id", id).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));

		assertEquals("", result.andReturn().getResponse().getContentAsString());
	}

	@Test
	public void testFindByIdOK() throws Exception {
		// Given
		UserMongoEntity entity = UserMongoEntity.builder()
				.id("1")
				.username("admin")
				.password("padmin")
				.role(RolesEnum.ADMIN.name())
				.language(Locale.ENGLISH.getLanguage())
				.languageBooks(Arrays.asList("spa", "eng"))
				.build();
		userRepository.save(entity);

		// When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/user/getById").param("id", entity.getId()).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(entity.getId()))
				.andExpect(jsonPath("$.username").value(entity.getUsername()))
				.andExpect(jsonPath("$.kindle").value(entity.getKindle()))
				.andExpect(jsonPath("$.role").value(entity.getRole()))
				.andExpect(jsonPath("$.language").value(entity.getLanguage()));
	}
}