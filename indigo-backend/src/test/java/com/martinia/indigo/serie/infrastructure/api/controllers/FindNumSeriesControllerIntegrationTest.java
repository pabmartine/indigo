package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindNumSeriesControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void findNumSeriesEmpty() throws Exception {
		// Given
		List<String> languages = List.of("en", "es");
		long numSeries = 0L;

		// When
		ResultActions resultActions = mockMvc.perform(
				get("/api/serie/count").param("languages", String.join(",", languages)).contentType(MediaType.APPLICATION_JSON));

		//Then
		resultActions.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(numSeries));
	}

	@Test
	public void findNumSeriesOK() throws Exception {
		// Given
		List<String> languages = List.of("en", "es");
		long numSeries = 2L;

		insertBook();
		insertOtherBook();

		// When
		ResultActions resultActions = mockMvc.perform(
				get("/api/serie/count").param("languages", String.join(",", languages)).contentType(MediaType.APPLICATION_JSON));

		//Then
		resultActions.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(numSeries));
	}

	private void insertOtherBook() {
		BookMongoEntity bookMongoEntity2 = BookMongoEntity.builder()
				.id("id2")
				.title("title2")
				.path("path2")
				.languages(List.of("es"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.serie(SerieMongo.builder().index(1).name("Serie2").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image("::image::")
				.build();
		bookRepository.save(bookMongoEntity2);
	}

	private void insertBook() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("id")
				.title("title")
				.path("path")
				.languages(List.of("es"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.serie(SerieMongo.builder().index(1).name("Serie1").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image("::image::")
				.build();
		bookRepository.save(bookMongoEntity);
	}
}
