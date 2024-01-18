package com.martinia.indigo.user.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.domain.model.RolesEnum;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateUserControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Resource
	private PasswordEncoder passwordEncoder;

	private UserMongoEntity entity;

	@BeforeEach
	public void init() {
		entity = UserMongoEntity.builder()
				.id("1")
				.username("test")
				.password("padmin")
				.role(RolesEnum.ADMIN.name())
				.language(Locale.ENGLISH.getLanguage())
				.languageBooks(Arrays.asList("spa", "eng"))
				.build();
		userRepository.save(entity);
	}

	@Test
	@WithMockUser
	public void testUpdateUserNotFound() throws Exception {

		//given
		User user = new User();
		user.setId("2");
		user.setUsername("testuser");
		user.setPassword("testpassword");
		String userJson = objectMapper.writeValueAsString(user);

		//When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.put("/api/user/update").contentType(MediaType.APPLICATION_JSON).content(userJson));

		//Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		assertTrue(userRepository.findById(user.getId()).isEmpty());

	}

	@Test
	@WithMockUser
	public void testUpdateUserOk() throws Exception {

		//given
		User user = new User();
		user.setId("1");
		user.setUsername("testuser");
		user.setPassword("testpassword");
		String userJson = objectMapper.writeValueAsString(user);

		//When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.put("/api/user/update").contentType(MediaType.APPLICATION_JSON).content(userJson));

		//Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		assertEquals(user.getUsername(), userRepository.findById(user.getId()).get().getUsername());

	}

}