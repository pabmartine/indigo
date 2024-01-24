package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.user.domain.model.RolesEnum;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FindAllUsersControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Test
	void testFindAllEmpty() throws Exception {
		// Given
		userRepository.deleteAll();

		// When
		ResultActions result = mockMvc.perform(get("/api/user/getAll").accept(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(status().isOk());
		assertTrue(userRepository.findAll().isEmpty());
	}

	@Test
	void testFindAllOk() throws Exception {
		// Given

		UserMongoEntity entity = UserMongoEntity.builder()
				.username("admin")
				.password("padmin")
				.role(RolesEnum.ADMIN.name())
				.language(Locale.ENGLISH.getLanguage())
				.languageBooks(Arrays.asList("spa", "eng"))
				.build();
		userRepository.save(entity);

		// When
		ResultActions result = mockMvc.perform(get("/api/user/getAll").accept(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(status().isOk());
		assertEquals(1, userRepository.count());
		UserMongoEntity user = userRepository.findAll().stream().findFirst().get();
		assertEquals(entity.getUsername(), user.getUsername());
		assertEquals(entity.getPassword(), user.getPassword());
		assertEquals(entity.getRole(), user.getRole());
		assertEquals(entity.getLanguage(), user.getLanguage());
		assertEquals(entity.getLanguageBooks().get(0), user.getLanguageBooks().get(0));
		assertEquals(entity.getLanguageBooks().get(1), user.getLanguageBooks().get(1));
	}

}