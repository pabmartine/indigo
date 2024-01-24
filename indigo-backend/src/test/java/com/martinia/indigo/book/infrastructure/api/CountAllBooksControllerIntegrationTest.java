package com.martinia.indigo.book.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.model.Search;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

class CountAllBooksControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private BookMongoEntity bookMongoEntity;

	private BookMongoEntity bookMongoEntity2;

	private SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

	private String UNKNOWN = "unknown";

	@BeforeEach
	@SneakyThrows
	public void init() {
		bookMongoEntity = BookMongoEntity.builder()
				.id("64dce11b1520b348ff4b96ae")
				.title("title")
				.path("path")
				.languages(List.of("spa"))
				.similar(Arrays.asList("similar"))
				.pubDate(SDF.parse("01/01/2000"))
				.authors(Arrays.asList("author"))
				.pages(100)
				.tags(Arrays.asList("tag"))
				.build();
		bookRepository.save(bookMongoEntity);

		bookMongoEntity2 = BookMongoEntity.builder().id("64dcec021520b348ff4be9d6").title("title2").languages(List.of("eng")).build();
		bookRepository.save(bookMongoEntity2);

	}

	@Test
	@WithMockUser
	void countAll() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/book/count/search/advance").contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(2));
	}

	@Test
	@WithMockUser
	void countAllFilteredByTitle() throws Exception {
		// Given
		final Search search = new Search();
		search.setTitle(bookMongoEntity.getTitle());
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
	}

	@Test
	@WithMockUser
	void countAllFilteredByTitleNotFound() throws Exception {
		// Given
		final Search search = new Search();
		search.setTitle(UNKNOWN);
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
	}

	@Test
	@WithMockUser
	void countAllFilteredByAuthor() throws Exception {
		// Given
		final Search search = new Search();
		search.setAuthor(bookMongoEntity.getAuthors().get(0));
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
	}

	@Test
	@WithMockUser
	void countAllFilteredByAuthorNotFound() throws Exception {
		// Given
		final Search search = new Search();
		search.setAuthor(UNKNOWN);
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
	}

	@Test
	@WithMockUser
	void countAllFilteredByPubdate() throws Exception {
		// Given
		final Search search = new Search();
		search.setIni(SDF.parse("01/01/1999"));
		search.setEnd(SDF.parse("01/01/2001"));
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
	}

	@Test
	@WithMockUser
	void countAllFilteredByPubdateNotFound() throws Exception {
		// Given
		final Search search = new Search();
		search.setIni(SDF.parse("01/01/2001"));
		search.setEnd(SDF.parse("01/01/2002"));
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
	}

	@Test
	@WithMockUser
	void countAllFilteredByPages() throws Exception {
		// Given
		final Search search = new Search();
		search.setMax(101);
		search.setMin(99);
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
	}

	@Test
	@WithMockUser
	void countAllFilteredByPagesNotFound() throws Exception {
		// Given
		final Search search = new Search();
		search.setMax(102);
		search.setMin(101);
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
	}

	@Test
	@WithMockUser
	void countAllFilteredByTags() throws Exception {
		// Given
		final Search search = new Search();
		search.setSelectedTags(bookMongoEntity.getTags());
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
	}

	@Test
	@WithMockUser
	void countAllFilteredByTagsNotFound() throws Exception {
		// Given
		final Search search = new Search();
		search.setSelectedTags(Arrays.asList(UNKNOWN));
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
	}

	@Test
	@WithMockUser
	void countAllFilteredByPath() throws Exception {
		// Given
		final Search search = new Search();
		search.setPath(bookMongoEntity.getPath());
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
	}

	@Test
	@WithMockUser
	void countAllFilteredByPathNotFound() throws Exception {
		// Given
		final Search search = new Search();
		search.setPath(UNKNOWN);
		search.setLanguages(Arrays.asList("spa"));
		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/count/search/advance")
				.content(new ObjectMapper().writeValueAsString(search))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
	}

}
