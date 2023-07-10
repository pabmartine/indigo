package com.martinia.indigo.author.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.favorite.DeleteFavoriteAuthorUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

public class DeleteFavoriteAuthorControllerTest extends BaseIndigoTest {

	@MockBean
	private DeleteFavoriteAuthorUseCase mockUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testDeleteFavoriteAuthors_WhenRequestParamsProvided_ThenReturnOkStatus() throws Exception {
		// Given
		String author = "John Doe";
		String user = "exampleUser";

		// When
		mockMvc.perform(MockMvcRequestBuilders.delete("/rest/author/favorite").param("author", author).param("user", user)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		Mockito.verify(mockUseCase).deleteFavoriteAuthor(user, author);
	}
}
