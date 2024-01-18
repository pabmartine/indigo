package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.Assert.assertTrue;

public class DeleteUserControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private UserMongoEntity userMongoEntity;

	@BeforeEach
	public void init() {
		userMongoEntity = userRepository.save(UserMongoEntity.builder().username("test").build());
	}

	@Test
	@WithMockUser
	public void testDeleteUserNotFound() throws Exception {

		//Given
		String userId = "12345";

		//When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/user/delete").param("id", userId).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		assertTrue(userRepository.findById(userMongoEntity.getId()).isPresent());

	}

	@Test
	@WithMockUser
	public void testDeleteUserNotOk() throws Exception {

		//Given
		//When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/delete")
				.param("id", userMongoEntity.getId())
				.contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		assertTrue(userRepository.findById(userMongoEntity.getId()).isEmpty());

	}
}
