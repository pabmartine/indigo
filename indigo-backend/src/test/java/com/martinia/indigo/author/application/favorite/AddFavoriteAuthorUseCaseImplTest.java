package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.favorite.AddFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;

class AddFavoriteAuthorUseCaseImplTest extends BaseIndigoTest {
	@Resource
	private AddFavoriteAuthorUseCase addFavoriteAuthorUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	void givenUserAndAuthor_whenAddFavoriteAuthor_thenRepositoryCalled() {
		// Given
		String user = "john";
		String author = "Stephen King";

		// When
		addFavoriteAuthorUseCase.addFavoriteAuthor(user, author);

		// Then
		verify(userRepository).addFavoriteAuthor(user, author);
	}
}