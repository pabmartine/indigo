package com.martinia.indigo.book.infrastructure.api.recommendation;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByUserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindBookRecommendationsByUserControllerTest extends BaseIndigoTest {

	@MockBean
	private FindBookRecommendationsByUserUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@InjectMocks
	private FindBookRecommendationsByUserController controller;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetBookRecommendationsByUser() throws Exception {
		// Given
		String user = "example_user";
		int page = 1;
		int size = 10;
		String sort = "title";
		String order = "asc";
		List<Book> books = Arrays.asList(new Book(), new Book());
		List<BookDto> bookDtos = Arrays.asList(new BookDto(), new BookDto());
		when(useCase.getRecommendationsByUser(user, page, size, sort, order)).thenReturn(books);
		when(mapper.domains2Dtos(books)).thenReturn(bookDtos);

		// When
		mockMvc.perform(
						MockMvcRequestBuilders.get("/rest/book/recommendations/user").param("user", user).param("page", String.valueOf(page))
								.param("size", String.valueOf(size)).param("sort", sort).param("order", order))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]").exists());

		// Then
		verify(useCase, times(1)).getRecommendationsByUser(user, page, size, sort, order);
		verify(mapper, times(1)).domains2Dtos(books);
	}

}