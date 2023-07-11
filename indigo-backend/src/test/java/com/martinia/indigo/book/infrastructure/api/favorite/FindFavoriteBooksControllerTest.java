package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.favorite.FindFavoriteBooksUseCase;
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

class FindFavoriteBooksControllerTest extends BaseIndigoTest {

	@MockBean
	private FindFavoriteBooksUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetFavoriteBooks() throws Exception {
		// Given
		String user = "example_user";
		List<Book> books = Arrays.asList(new Book(), new Book());
		List<BookDto> bookDtos = Arrays.asList(new BookDto(), new BookDto());
		when(useCase.getFavoriteBooks(user)).thenReturn(books);
		when(mapper.domains2Dtos(books)).thenReturn(bookDtos);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/book/favorites").param("user", user))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]").exists());

		// Then
		verify(useCase, times(1)).getFavoriteBooks(user);
		verify(mapper, times(1)).domains2Dtos(books);
	}

}