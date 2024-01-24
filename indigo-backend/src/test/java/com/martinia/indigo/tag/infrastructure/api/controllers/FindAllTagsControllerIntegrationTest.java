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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindAllTagsControllerIntegrationTest extends BaseIndigoIntegrationTest {

	public static final String ID_1 = "id1";
	public static final String NAME_1 = "name1";
	public static final String IMAGE_1 = "image1";
	public static final String ID_2 = "id2";
	public static final String NAME_2 = "name2";
	public static final String IMAGE_2 = "image2";
	public static final int TWO = 2;
	public static final int ONE = 1;

	@Test
	public void findAllTagsEmpty() throws Exception {
		// Given
		String sort = "name";
		String order = "asc";

		// When
		ResultActions result = mockMvc.perform(get("/api/tag/all").param("languages", "en", "es")
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(0));
	}

	@Test
	public void findAllTagsOrderAscOk() throws Exception {
		// Given
		String sort = "name";
		String order = "asc";

		insertTag();
		insertOtherTag();

		// When
		ResultActions result = mockMvc.perform(get("/api/tag/all").param("languages", "en", "es")
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		//Then

		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(TWO))
				.andExpect(jsonPath("$[0].id").value(ID_1))
				.andExpect(jsonPath("$[0].name").value(NAME_1))
				.andExpect(jsonPath("$[0].image").value(IMAGE_1))
				.andExpect(jsonPath("$[0].numBooks").value(ONE))
				.andExpect(jsonPath("$[1].id").value(ID_2))
				.andExpect(jsonPath("$[1].name").value(NAME_2))
				.andExpect(jsonPath("$[1].image").value(IMAGE_2))
				.andExpect(jsonPath("$[1].numBooks").value(ONE));
	}

	@Test
	public void findAllTagsOrderDescOk() throws Exception {
		// Given
		String sort = "name";
		String order = "desc";

		insertTag();
		insertOtherTag();

		// When
		ResultActions result = mockMvc.perform(get("/api/tag/all").param("languages", "en", "es")
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		//Then

		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(TWO))
				.andExpect(jsonPath("$[1].id").value(ID_1))
				.andExpect(jsonPath("$[1].name").value(NAME_1))
				.andExpect(jsonPath("$[1].image").value(IMAGE_1))
				.andExpect(jsonPath("$[1].numBooks").value(ONE))
				.andExpect(jsonPath("$[0].id").value(ID_2))
				.andExpect(jsonPath("$[0].name").value(NAME_2))
				.andExpect(jsonPath("$[0].image").value(IMAGE_2))
				.andExpect(jsonPath("$[0].numBooks").value(ONE));
	}

	@Test
	public void findAllTagsLanguageOk() throws Exception {
		// Given
		String sort = "name";
		String order = "asc";

		insertTag();
		insertOtherTag();

		// When
		ResultActions result = mockMvc.perform(get("/api/tag/all").param("languages", "es")
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		//Then

		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(ONE))
				.andExpect(jsonPath("$[0].id").value(ID_1))
				.andExpect(jsonPath("$[0].name").value(NAME_1))
				.andExpect(jsonPath("$[0].image").value(IMAGE_1))
				.andExpect(jsonPath("$[0].numBooks").value(ONE));
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

	private void insertOtherTag() {
		final Map<String, Integer> languages = new HashMap<>();
		languages.put("en", ONE);
		final TagMongoEntity tagEntity = TagMongoEntity.builder()
				.id(ID_2)
				.image(IMAGE_2)
				.name(NAME_2)
				.numBooks(NumBooksMongo.builder().languages(languages).total(ONE).build())
				.build();
		tagRepository.save(tagEntity);
	}
}
