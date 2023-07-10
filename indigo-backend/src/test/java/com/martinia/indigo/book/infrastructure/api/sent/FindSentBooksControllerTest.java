package com.martinia.indigo.book.infrastructure.api.sent;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.sent.FindSentBooksUseCase;
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

class FindSentBooksControllerTest extends BaseIndigoTest {

	@MockBean
	private FindSentBooksUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetSentBooks() throws Exception {
		// Given
		String user = "example_user";
		List<Book> books = Arrays.asList(new Book(), new Book());
		List<BookDto> bookDtos = Arrays.asList(new BookDto(), new BookDto());
		when(useCase.getSentBooks(user)).thenReturn(books);
		when(mapper.domains2Dtos(books)).thenReturn(bookDtos);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/book/sent").param("user", user)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists()).andExpect(MockMvcResultMatchers.jsonPath("$[1]").exists());

		// Then
		verify(useCase, times(1)).getSentBooks(user);
		verify(mapper, times(1)).domains2Dtos(books);
	}

}