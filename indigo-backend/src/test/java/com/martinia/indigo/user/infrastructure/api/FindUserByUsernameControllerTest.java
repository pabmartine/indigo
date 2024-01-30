package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.domain.model.RolesEnum;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Locale;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindUserByUsernameControllerTest extends BaseIndigoIntegrationTest {

	@Test
	public void testFindByUsernameNotExist() throws Exception {
		// Given
		String username = "username";

		// When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/user/get").param("username", username).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());

		Assert.assertEquals("", result.andReturn().getResponse().getContentAsString());
	}

	@Test
	public void testFindByUsernameOK() throws Exception {
		// Given
		UserMongoEntity entity = UserMongoEntity.builder()
				.id("1")
				.username("test")
				.password("padmin")
				.role(RolesEnum.ADMIN.name())
				.language(Locale.ENGLISH.getLanguage())
				.languageBooks(Arrays.asList("spa", "eng"))
				.build();
		userRepository.save(entity);

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/user/get")
				.param("username", entity.getUsername())
				.contentType(MediaType.APPLICATION_JSON));

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