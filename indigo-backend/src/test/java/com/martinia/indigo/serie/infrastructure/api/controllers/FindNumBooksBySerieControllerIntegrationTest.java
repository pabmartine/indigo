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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindNumBooksBySerieControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void findNumBooksBySerieEmpty() throws Exception {
		// Given
		List<String> languages = List.of("en", "es");
		int page = 1;
		int size = 10;
		String sort = "title";
		String order = "asc";
		Map<String, Long> data = new HashMap<>();
		data.put("Serie1", 5L);
		data.put("Serie2", 3L);

		// When
		ResultActions resultActions = mockMvc.perform(get("/api/serie/all").param("languages", String.join(",", languages))
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0)).andReturn();
	}

	@Test
	public void findNumBooksBySerieOK() throws Exception {
		// Given

		insertBook();

		List<String> languages = List.of("en", "es");
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		// When
		ResultActions resultActions = mockMvc.perform(get("/api/serie/all").param("languages", String.join(",", languages))
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].numBooks", Matchers.is("1")))
				.andExpect(jsonPath("$[0].name", Matchers.is("Serie1")))
				.andReturn();
	}

	@Test
	public void findNumBooksBySerieOrderAscOK() throws Exception {
		// Given

		insertBook();

		insertOtherBook();

		List<String> languages = List.of("en", "es");
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		// When
		ResultActions resultActions = mockMvc.perform(get("/api/serie/all").param("languages", String.join(",", languages))
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].numBooks", Matchers.is("1")))
				.andExpect(jsonPath("$[0].name", Matchers.is("Serie1")))
				.andExpect(jsonPath("$[1].numBooks", Matchers.is("1")))
				.andExpect(jsonPath("$[1].name", Matchers.is("Serie2")))
				.andReturn();
	}

	@Test
	public void findNumBooksBySerieOrderDescOK() throws Exception {
		// Given

		insertBook();

		insertOtherBook();

		List<String> languages = List.of("en", "es");
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "desc";

		// When
		ResultActions resultActions = mockMvc.perform(get("/api/serie/all").param("languages", String.join(",", languages))
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].numBooks", Matchers.is("1")))
				.andExpect(jsonPath("$[0].name", Matchers.is("Serie2")))
				.andExpect(jsonPath("$[1].numBooks", Matchers.is("1")))
				.andExpect(jsonPath("$[1].name", Matchers.is("Serie1")))
				.andReturn();
	}

	@Test
	public void findNumBooksBySerieLimitOK() throws Exception {
		// Given

		insertBook();

		insertOtherBook();

		List<String> languages = List.of("en", "es");
		int page = 1;
		int size = 1;
		String sort = "title";
		String order = "desc";

		// When
		ResultActions resultActions = mockMvc.perform(get("/api/serie/all").param("languages", String.join(",", languages))
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].numBooks", Matchers.is("1")))
				.andExpect(jsonPath("$[0].name", Matchers.is("Serie1")))
				.andReturn();
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
