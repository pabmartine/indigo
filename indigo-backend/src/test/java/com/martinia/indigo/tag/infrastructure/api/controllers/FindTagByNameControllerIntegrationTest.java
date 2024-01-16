package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindTagByNameControllerIntegrationTest extends BaseIndigoIntegrationTest {

	public static final String ID_1 = "id1";
	public static final String NAME_1 = "name1";
	public static final String IMAGE_1 = "image1";
	public static final int ONE = 1;

	@Resource
	private MockMvc mockMvc;

	@Test
	public void testGetTagByNameNotExist() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(get("/api/tag/tag").param("name", NAME_1).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());
		assertEquals("", result.andReturn().getResponse().getContentAsString());

	}

	@Test
	public void testGetTagByNameOk() throws Exception {
		// Given

		insertTag();
		// When
		ResultActions result = mockMvc.perform(get("/api/tag/tag").param("name", NAME_1).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").value(ID_1));
	}

	private void insertTag() {
		final Map<String, Integer> languages = new HashMap<>();
		languages.put("es", ONE);
		final TagMongoEntity tagEntity = TagMongoEntity.builder()
				.id(ID_1)
				.image(IMAGE_1)
				.name(NAME_1)
				.numBooks(NumBooksMongo.builder().languages(languages).total(ONE).build())
				.build();
		tagRepository.save(tagEntity);
	}
}
