package com.martinia.indigo.book.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
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
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

class FindAllBooksControllerIntegrationTest extends BaseIndigoIntegrationTest {

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

		bookMongoEntity2 = BookMongoEntity.builder().id("64dcec021520b348ff4be9d6").title("title2").languages(List.of("spa")).build();
		bookRepository.save(bookMongoEntity2);

	}

	@Test
	@WithMockUser
	public void findAll() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setTitle(bookMongoEntity.getTitle());
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(2, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());
		assertEquals(bookMongoEntity2.getId(), list.get(1).getId());
	}

	@Test
	@WithMockUser
	public void findAllDesc() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "desc";

		final Search search = new Search();
		search.setTitle(bookMongoEntity.getTitle());
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(2, list.size());
		assertEquals(bookMongoEntity2.getId(), list.get(0).getId());
		assertEquals(bookMongoEntity.getId(), list.get(1).getId());
	}

	@Test
	@WithMockUser
	public void findAllLimit() throws Exception {
		// Given
		int page = 0;
		int size = 1;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setTitle(bookMongoEntity.getTitle());
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());
	}

	@Test
	@WithMockUser
	public void findAllPage() throws Exception {
		// Given
		int page = 1;
		int size = 1;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setTitle(bookMongoEntity.getTitle());
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity2.getId(), list.get(0).getId());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByTitle() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setTitle(bookMongoEntity.getTitle());
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(2, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());
		assertEquals(bookMongoEntity2.getId(), list.get(1).getId());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByTitleNotFound() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setTitle(UNKNOWN);
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(0, list.size());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByAuthor() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setAuthor(bookMongoEntity.getAuthors().get(0));
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByAuthorNotFound() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setAuthor(UNKNOWN);
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(0, list.size());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByPubDate() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setIni(SDF.parse("01/01/1999"));
		search.setEnd(SDF.parse("01/01/2001"));
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByPubDateNotFound() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setIni(SDF.parse("01/01/2001"));
		search.setEnd(SDF.parse("01/01/2002"));
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(0, list.size());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByPages() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setMax(101);
		search.setMin(99);
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByPagesNotFound() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setMax(102);
		search.setMin(101);
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(0, list.size());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByTags() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setSelectedTags(bookMongoEntity.getTags());
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByTagsNotFound() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setSelectedTags(Arrays.asList(UNKNOWN));
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(0, list.size());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByPath() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setPath(bookMongoEntity.getPath());
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(1, list.size());
		assertEquals(bookMongoEntity.getId(), list.get(0).getId());
	}

	@Test
	@WithMockUser
	public void findAllFilteredByPathNotFound() throws Exception {
		// Given
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		final Search search = new Search();
		search.setPath(UNKNOWN);
		search.setLanguages(Arrays.asList("spa"));

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance")
				.contentType(MediaType.APPLICATION_JSON)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.param("sort", sort)
				.param("order", order)
				.content(new ObjectMapper().writeValueAsString(search)));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		List<BookDto> list = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper().readValue(
				result.andReturn().getResponse().getContentAsString(), new TypeReference<List<BookDto>>() {});
		assertEquals(0, list.size());
	}
}
