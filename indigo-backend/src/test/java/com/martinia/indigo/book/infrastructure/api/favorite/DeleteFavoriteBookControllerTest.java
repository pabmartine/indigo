package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.favorite.DeleteFavoriteBookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DeleteFavoriteBookControllerTest extends BaseIndigoTest {

	@MockBean
	private DeleteFavoriteBookUseCase useCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	void testDeleteFavoriteBooks() throws Exception {
		// Given
		String user = "example_user";
		String book = "example_book";

		// When
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/book/favorite").param("user", user).param("book", book))
				.andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		verify(useCase, times(1)).deleteFavoriteBook(user, book);
	}

}