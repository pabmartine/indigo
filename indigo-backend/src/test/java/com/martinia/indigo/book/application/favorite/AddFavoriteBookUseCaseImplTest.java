package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.favorite.AddFavoriteBookUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddFavoriteBookUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private AddFavoriteBookUseCase addFavoriteBookUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void testAddFavoriteBook_WhenUserHasNoFavoriteBooks_AddsBookToUserFavorites() {
		// Given
		String user = "john_doe";
		String book = "Book 1";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		userEntity.setFavoriteBooks(new ArrayList<>());

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));

		// When
		addFavoriteBookUseCase.addFavoriteBook(user, book);

		// Then
		verify(userRepository).save(userEntity);
		List<String> expectedFavoriteBooks = Arrays.asList(book);
		assert (userEntity.getFavoriteBooks()
				.equals(expectedFavoriteBooks));
	}

	@Test
	public void testAddFavoriteBook_WhenUserAlreadyHasFavoriteBook_DoesNotAddBookToUserFavorites() {
		// Given
		String user = "john_doe";
		String book = "Book 1";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		userEntity.setFavoriteBooks(Arrays.asList(book));

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));

		// When
		addFavoriteBookUseCase.addFavoriteBook(user, book);

		// Then
		verify(userRepository).findByUsername(user);
		verify(userRepository).save(any());
	}
}

