package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.favorite.CheckIsFavoriteBookUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckIsFavoriteBookUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private UserRepository userRepository;

	@Resource
	private CheckIsFavoriteBookUseCase useCase;

	@Test
	void testIsFavoriteBook() {
		// Given
		String user = "example_user";
		String book = "example_book";
		Boolean expectedIsFavorite = true;
		when(userRepository.isFavoriteBook(user, book)).thenReturn(expectedIsFavorite);

		// When
		Boolean actualIsFavorite = useCase.isFavoriteBook(user, book);

		// Then
		assertEquals(expectedIsFavorite, actualIsFavorite);
		verify(userRepository, times(1)).isFavoriteBook(user, book);
	}

}