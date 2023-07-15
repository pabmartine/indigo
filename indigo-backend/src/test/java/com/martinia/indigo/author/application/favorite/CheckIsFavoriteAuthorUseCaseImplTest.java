package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.favorite.CheckIsFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class CheckIsFavoriteAuthorUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private CheckIsFavoriteAuthorUseCase checkIsFavoriteAuthorUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void testIsFavoriteAuthor_UserExistsAndAuthorIsFavorite_ReturnsTrue() {
		// Given
		String username = "john_doe";
		String author = "Jane Smith";

		UserMongoEntity userEntity = UserMongoEntity.builder().username(username).favoriteAuthors(Collections.singletonList(author))
				.build();

		when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

		// When
		boolean result = checkIsFavoriteAuthorUseCase.isFavoriteAuthor(username, author);

		// Then
		assertTrue(result);
	}

	@Test
	public void testIsFavoriteAuthor_UserExistsAndAuthorIsNotFavorite_ReturnsFalse() {
		// Given
		String username = "john_doe";
		String author = "Jane Smith";
		String otherAuthor = "John Doe";

		UserMongoEntity userEntity = UserMongoEntity.builder().username(username).favoriteAuthors(Collections.singletonList(otherAuthor))
				.build();

		when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

		// When
		boolean result = checkIsFavoriteAuthorUseCase.isFavoriteAuthor(username, author);

		// Then
		assertFalse(result);
	}

	@Test
	public void testIsFavoriteAuthor_UserDoesNotExist_ReturnsFalse() {
		// Given
		String username = "non_existing_user";
		String author = "Jane Smith";

		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

		// When
		boolean result = checkIsFavoriteAuthorUseCase.isFavoriteAuthor(username, author);

		// Then
		assertFalse(result);
	}
}

