package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MergeTagControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	public static final String ID_1 = "id1";
	public static final String NAME_1 = "name1";
	public static final String IMAGE_1 = "image1";
	public static final String ID_2 = "id2";
	public static final String NAME_2 = "name2";
	public static final String IMAGE_2 = "image2";
	public static final int ONE = 1;

	public static final String UNKNOWN = "UNKNOWN";

	@BeforeEach
	public void init(){
		insertTag();
		insertOtherTag();
		insertBookWithTag();
		insertBookWithOtherTag();
	}



	@Test
	public void mergeTagNoSource() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(
				get("/api/tag/merge").param("source", UNKNOWN).param("target", ID_2).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk()).andExpect(content().string(""));
	}

	@Test
	public void mergeTagNoTarget() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(
				get("/api/tag/merge").param("source", ID_1).param("target", UNKNOWN).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk()).andExpect(content().string(""));
	}

	@Test
	public void mergeTagOk() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(
				get("/api/tag/merge").param("source", ID_1).param("target", ID_2).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk()).andExpect(content().string(""));

		assertTrue(tagRepository.findById(ID_1).isEmpty());
		assertEquals(2, tagRepository.findById(ID_2).get().getNumBooks().getTotal());
		assertTrue(bookRepository.findById("64dce11b1520b348ff4b96ae").get().getTags().contains(NAME_2));
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

	@SneakyThrows
	private void insertBookWithOtherTag() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("37dce11b1520b348ff4b96ae")
				.title("title2")
				.path("path2")
				.languages(List.of("spa"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author2"))
				.pages(100)
				.tags(Arrays.asList(NAME_2))
				.build();
		bookRepository.save(bookMongoEntity);
	}
}
