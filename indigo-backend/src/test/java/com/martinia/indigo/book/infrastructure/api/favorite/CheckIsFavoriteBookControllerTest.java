package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.favorite.CheckIsFavoriteBookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckIsFavoriteBookControllerTest extends BaseIndigoTest {

	@MockBean
	private CheckIsFavoriteBookUseCase useCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testIsFavoriteBook() throws Exception {
		// Given
		String user = "example_user";
		String book = "example_book";
		Boolean isFavorite = true;
		when(useCase.isFavoriteBook(user, book)).thenReturn(isFavorite);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/book/favorite").param("user", user).param("book", book))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(isFavorite.toString()));

		// Then
		verify(useCase, times(1)).isFavoriteBook(user, book);
	}

}