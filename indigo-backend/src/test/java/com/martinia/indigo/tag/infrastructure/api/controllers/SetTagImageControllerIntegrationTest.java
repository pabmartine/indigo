package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SetTagImageControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private static final String BASE_PATH = "/api/tag";

	public static final String ID_1 = "id1";
	public static final String NAME_1 = "name1";
	public static final String IMAGE_1 = "image1";
	public static final int ONE = 1;

	@Test
	public void testSetImageTagNotExist() throws Exception {
		// Given
		String source = "sourceTag";
		String image = "tagImage";

		insertTag();

		// When
		ResultActions result = mockMvc.perform(
				get(BASE_PATH + "/image").param("source", source).param("image", image).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());

		assertEquals(IMAGE_1, tagRepository.findById(ID_1).get().getImage());

	}

	@Test
	public void testSetImageTagOk() throws Exception {
		// Given
		String image = "tagImage";

		insertTag();

		// When
		ResultActions result = mockMvc.perform(
				get(BASE_PATH + "/image").param("source", ID_1).param("image", image).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());

		assertEquals(image, tagRepository.findById(ID_1).get().getImage());

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
