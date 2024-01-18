package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.Assert.assertTrue;

public class SaveUserControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testSaveUser() throws Exception {

		//Given
		User user = new User();
		user.setUsername("testuser");
		user.setPassword("password");

		//When

		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/user/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"testuser\",\"password\":\"password\"}"));

		//Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		assertTrue(userRepository.findByUsername(user.getUsername()).isPresent());
	}
}