package com.martinia.indigo.book.infrastructure.api.similar;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.similar.FindSimilarBooksUseCase;
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

class FindSimilarBooksControllerTest extends BaseIndigoTest {

	@MockBean
	private FindSimilarBooksUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@InjectMocks
	private FindSimilarBooksController controller;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetSimilar() throws Exception {
		// Given
		List<String> similarBooks = Arrays.asList("similar1", "similar2");
		List<String> languages = Arrays.asList("English", "Spanish");
		List<Book> books = Arrays.asList(new Book(), new Book());
		List<BookDto> bookDtos = Arrays.asList(new BookDto(), new BookDto());
		when(useCase.getSimilar(similarBooks, languages)).thenReturn(books);
		when(mapper.domains2Dtos(books)).thenReturn(bookDtos);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/book/similar").param("similar", "similar1", "similar2")
						.param("languages", "English", "Spanish")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists()).andExpect(MockMvcResultMatchers.jsonPath("$[1]").exists());

		// Then
		verify(useCase, times(1)).getSimilar(similarBooks, languages);
		verify(mapper, times(1)).domains2Dtos(books);
	}

}