package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.favorite.AddFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddFavoriteAuthorUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private AddFavoriteAuthorUseCase addFavoriteAuthorUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void testAddFavoriteAuthor_UserExists_AddsFavoriteAuthor() {
		// Given
		String username = "john_doe";
		String author = "Jane Smith";

		UserMongoEntity existingUser = new UserMongoEntity();
		existingUser.setUsername(username);
		existingUser.setFavoriteAuthors(new ArrayList<>());

		when(userRepository.findByUsername(any())).thenReturn(Optional.of(existingUser));

		// When
		addFavoriteAuthorUseCase.addFavoriteAuthor(username, author);

		// Then
		assertEquals(Arrays.asList(author), existingUser.getFavoriteAuthors());
		verify(userRepository, atLeast(1)).save(any());
	}

	@Test
	public void testAddFavoriteAuthor_UserDoesNotExist_DoesNotAddFavoriteAuthor() {
		// Given
		String username = "non_existing_user";
		String author = "Jane Smith";

		when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

		// When
		addFavoriteAuthorUseCase.addFavoriteAuthor(username, author);

		// Then
		verify(userRepository).findByUsername(username);
	}
}
