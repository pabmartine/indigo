package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.favorite.CheckIsFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CheckIsFavoriteAuthorUseCaseImplTest extends BaseIndigoTest {
	@Resource
	private CheckIsFavoriteAuthorUseCase checkIsFavoriteAuthorUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	void givenUserAndAuthor_whenIsFavoriteAuthor_thenReturnTrue() {
		// Given
		String user = "john";
		String author = "Stephen King";
		when(userRepository.isFavoriteAuthor(user, author)).thenReturn(true);

		// When
		Boolean isFavorite = checkIsFavoriteAuthorUseCase.isFavoriteAuthor(user, author);

		// Then
		assertEquals(true, isFavorite);
	}

	@Test
	void givenUserAndAuthor_whenIsFavoriteAuthor_thenReturnFalse() {
		// Given
		String user = "john";
		String author = "Stephen King";
		when(userRepository.isFavoriteAuthor(user, author)).thenReturn(false);

		// When
		Boolean isFavorite = checkIsFavoriteAuthorUseCase.isFavoriteAuthor(user, author);

		// Then
		assertEquals(false, isFavorite);
	}
}
