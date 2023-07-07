package com.martinia.indigo.author.infrastructure.api.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.service.favorite.CheckIsFavoriteAuthorUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

class CheckIsFavoriteAuthorControllerTest extends BaseIndigoTest {

	@MockBean
	private CheckIsFavoriteAuthorUseCase mockUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testGetFavoriteAuthor_WhenRequestParamsProvided_ThenReturnOkStatusAndIsFavorite() throws Exception {
		// Given
		String author = "John Doe";
		String user = "exampleUser";
		boolean isFavorite = true;
		Mockito.when(mockUseCase.isFavoriteAuthor(user, author)).thenReturn(isFavorite);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/author/favorite").param("author", author).param("user", user)
						.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(isFavorite)));

		// Then
		Mockito.verify(mockUseCase).isFavoriteAuthor(user, author);
	}
}

