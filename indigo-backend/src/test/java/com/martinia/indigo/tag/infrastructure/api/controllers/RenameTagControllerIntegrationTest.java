package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RenameTagControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private static final String BASE_PATH = "/api/tag";
	public static final String ID_1 = "id1";
	public static final String NAME_1 = "name1";
	public static final String IMAGE_1 = "image1";
	public static final int ONE = 1;

	@Test
	public void testRenameNotExist() throws Exception {
		// Given
		String source = "sourceTag";
		String target = "targetTag";

		// When
		ResultActions result = mockMvc.perform(
				get(BASE_PATH + "/rename").param("source", source).param("target", target).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());

		assertTrue(tagRepository.findById(source).isEmpty());
		assertTrue(tagRepository.findById(target).isEmpty());
	}

	@Test
	public void testRenameOk() throws Exception {
		// Given
		String target = "targetTag";

		insertTag();
		insertBookWithTag();

		// When
		ResultActions result = mockMvc.perform(
				get(BASE_PATH + "/rename").param("source", ID_1).param("target", target).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());

		assertNotEquals(NAME_1, tagRepository.findById(ID_1).get().getName());
		assertEquals(target, tagRepository.findById(ID_1).get().getName());
		assertEquals(1, bookRepository.findByTag(target).size());
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

	@SneakyThrows
	private void insertBookWithTag() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("64dce11b1520b348ff4b96ae")
				.title("title")
				.path("path")
				.languages(List.of("spa"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.pages(100)
				.tags(Arrays.asList(NAME_1))
				.build();
		bookRepository.save(bookMongoEntity);
	}

}
