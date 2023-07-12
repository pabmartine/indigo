package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.favorite.DeleteFavoriteBookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DeleteFavoriteBookUseCaseImplTest extends BaseIndigoTest {

//	@MockBean
//	private UserMongoRepository userMongoRepository;

	@Resource
	private DeleteFavoriteBookUseCase useCase;

	@Test
	void testDeleteFavoriteBook() {
		// Given
		String user = "example_user";
		String book = "example_book";

		// When
		useCase.deleteFavoriteBook(user, book);

		// Then
//		verify(userRepository, times(1)).deleteFavoriteBook(user, book);
	}

}