package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.service.favorite.DeleteFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;

class DeleteFavoriteAuthorUseCaseImplTest extends BaseIndigoTest {
	@Resource
	private DeleteFavoriteAuthorUseCase deleteFavoriteAuthorUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	void givenUserAndAuthor_whenDeleteFavoriteAuthor_thenRepositoryCalled() {
		// Given
		String user = "john";
		String author = "Stephen King";

		// When
		deleteFavoriteAuthorUseCase.deleteFavoriteAuthor(user, author);

		// Then
		verify(userRepository).deleteFavoriteAuthor(user, author);
	}
}
