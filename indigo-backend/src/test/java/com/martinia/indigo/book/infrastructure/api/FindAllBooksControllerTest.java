package com.martinia.indigo.book.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.FindAllBooksUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FindAllBooksControllerTest extends BaseIndigoTest {

	@InjectMocks
	private FindAllBooksController findAllBooksController;

	@MockBean
	private FindAllBooksUseCase findAllBooksUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testGetBooks() throws Exception {
		// Given
		int page = 1;
		int size = 10;
		String sort = "title";
		String order = "asc";
		List<Book> expectedBooks = new ArrayList<>();
		// Add some sample books to the expectedBooks list
		expectedBooks.add(Book.builder().title("testTitle").build());

		// Mock the behavior of the findAllBooksUseCase
		when(findAllBooksUseCase.findAll(any(), anyInt(), anyInt(), any(), any())).thenReturn(expectedBooks);

		// When
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/all/advance").contentType(MediaType.APPLICATION_JSON)
						.param("page", String.valueOf(page)).param("size", String.valueOf(size)).param("sort", sort).param("order", order))
				.andExpect(status().isOk()).andReturn();

		// Then
		String responseBody = result.getResponse().getContentAsString();

		ObjectMapper mapper = new ObjectMapper();
		List<BookDto> list = Arrays.asList(mapper.readValue(responseBody, BookDto[].class));

		Assertions.assertEquals(list.get(0).getTitle(), expectedBooks.get(0).getTitle());
	}

}