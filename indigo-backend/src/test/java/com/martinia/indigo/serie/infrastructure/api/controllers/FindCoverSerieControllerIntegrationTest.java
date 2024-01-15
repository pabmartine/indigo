package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import org.hamcrest.Matchers;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindCoverSerieControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void findCoverSerieNotFound() throws Exception {
		// Given
		String serie = "TestSerie";

		// When
		ResultActions resultActions = mockMvc.perform(
				get("/api/serie/cover").param("serie", serie).contentType(MediaType.APPLICATION_JSON));
		resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.image").doesNotExist());

	}

	@Test
	public void findCoverSerieOk() throws Exception {
		// Given
		String serie = "TestSerie";

		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("64dce11b1520b348ff4b96ae")
				.title("title")
				.path("path")
				.languages(List.of("spa"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.serie(SerieMongo.builder().index(1).name("TestSerie").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image("::image::")
				.build();
		bookRepository.save(bookMongoEntity);

		// When
		ResultActions resultActions = mockMvc.perform(
				get("/api/serie/cover").param("serie", serie).contentType(MediaType.APPLICATION_JSON));
		resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.image", Matchers.is("::image::")));

	}
}
