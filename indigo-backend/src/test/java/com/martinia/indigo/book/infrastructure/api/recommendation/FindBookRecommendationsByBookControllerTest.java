package com.martinia.indigo.book.infrastructure.api.recommendation;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByBookUseCase;
import org.junit.jupiter.api.Test;
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

class FindBookRecommendationsByBookControllerTest extends BaseIndigoTest {
	@MockBean
	private FindBookRecommendationsByBookUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetBookRecommendationsByBook() throws Exception {
		// Given
		List<String> recommendations = Arrays.asList("recommendation1", "recommendation2");
		List<String> languages = Arrays.asList("English", "Spanish");
		List<Book> books = Arrays.asList(new Book(), new Book());
		List<BookDto> bookDtos = Arrays.asList(new BookDto(), new BookDto());
		when(useCase.getRecommendationsByBook(recommendations, languages)).thenReturn(books);
		when(mapper.domains2Dtos(books)).thenReturn(bookDtos);

		// When
		mockMvc.perform(
						MockMvcRequestBuilders.get("/api/book/recommendations/book").param("recommendations", "recommendation1", "recommendation2")
								.param("languages", "English", "Spanish")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists()).andExpect(MockMvcResultMatchers.jsonPath("$[1]").exists());

		// Then
		verify(useCase, times(1)).getRecommendationsByBook(recommendations, languages);
		verify(mapper, times(1)).domains2Dtos(books);
	}
}