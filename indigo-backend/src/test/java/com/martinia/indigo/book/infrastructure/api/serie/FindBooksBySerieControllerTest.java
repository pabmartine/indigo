package com.martinia.indigo.book.infrastructure.api.serie;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.api.controllers.serie.FindBooksBySerieController;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.serie.FindBooksBySerieUseCase;
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

class FindBooksBySerieControllerTest extends BaseIndigoTest {

	@MockBean
	private FindBooksBySerieUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@InjectMocks
	private FindBooksBySerieController controller;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetSerie() throws Exception {
		// Given
		String serie = "example_serie";
		List<String> languages = Arrays.asList("English", "Spanish");
		List<Book> books = Arrays.asList(new Book(), new Book());
		List<BookDto> bookDtos = Arrays.asList(new BookDto(), new BookDto());
		when(useCase.getSerie(serie, languages)).thenReturn(books);
		when(mapper.domains2Dtos(books)).thenReturn(bookDtos);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/book/serie").param("serie", serie).param("languages", "English", "Spanish"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]").exists());

		// Then
		verify(useCase, times(1)).getSerie(serie, languages);
		verify(mapper, times(1)).domains2Dtos(books);
	}

}