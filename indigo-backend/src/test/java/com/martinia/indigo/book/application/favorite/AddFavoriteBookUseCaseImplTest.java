package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.service.favorite.AddFavoriteBookUseCase;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AddFavoriteBookUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private UserRepository userRepository;

	@Resource
	private AddFavoriteBookUseCase useCase;

	@Test
	void testAddFavoriteBook() {
		// Given
		String user = "example_user";
		String book = "example_book";

		// When
		useCase.addFavoriteBook(user, book);

		// Then
		verify(userRepository, times(1)).addFavoriteBook(user, book);
	}

}
