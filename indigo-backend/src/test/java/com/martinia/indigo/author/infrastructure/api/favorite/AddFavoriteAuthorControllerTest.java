package com.martinia.indigo.author.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.service.favorite.AddFavoriteAuthorUseCase;
import com.martinia.indigo.book.domain.model.Book;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

class AddFavoriteAuthorControllerTest extends BaseIndigoTest {

	@MockBean
	private AddFavoriteAuthorUseCase mockUseCase;

	@MockBean
	private Book mockBook;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testAddFavoriteAuthors_WhenRequestParamsProvided_ThenReturnOkStatus() throws Exception {
		// Given
		String author = "John Doe";
		String user = "exampleUser";

		// When
		mockMvc.perform(MockMvcRequestBuilders.post("/rest/author/favorite").param("author", author).param("user", user)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		Mockito.verify(mockUseCase).addFavoriteAuthor(user, author);
	}

}