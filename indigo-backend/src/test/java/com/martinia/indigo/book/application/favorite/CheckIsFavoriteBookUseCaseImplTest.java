package com.martinia.indigo.book.application.favorite;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.favorite.CheckIsFavoriteBookUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class CheckIsFavoriteBookUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private CheckIsFavoriteBookUseCase checkIsFavoriteBookUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void testIsFavoriteBook_WithExistingFavoriteBook_ReturnsTrue() {
		// Given
		String user = "john_doe";
		String book = "Book 1";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		userEntity.setFavoriteBooks(Arrays.asList("Book 1", "Book 2", "Book 3"));

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));

		// When
		boolean result = checkIsFavoriteBookUseCase.isFavoriteBook(user, book);

		// Then
		assertTrue(result);
	}

	@Test
	public void testIsFavoriteBook_WithNonExistingFavoriteBook_ReturnsFalse() {
		// Given
		String user = "john_doe";
		String book = "Book 4";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		userEntity.setFavoriteBooks(Arrays.asList("Book 1", "Book 2", "Book 3"));

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));

		// When
		boolean result = checkIsFavoriteBookUseCase.isFavoriteBook(user, book);

		// Then
		assertFalse(result);
	}

}
