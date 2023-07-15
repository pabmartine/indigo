package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.favorite.DeleteFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteFavoriteAuthorUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private DeleteFavoriteAuthorUseCase deleteFavoriteAuthorUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void testDeleteFavoriteAuthor_RemovesAuthorFromFavoriteAuthors() {
		// Given
		String user = "john_doe";
		String author = "Jane Smith";

		UserMongoEntity userEntity = Mockito.mock(UserMongoEntity.class);
		List<String> list = new ArrayList<>();
		list.add(author);
		when(userEntity.getFavoriteAuthors()).thenReturn(list);

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));

		// When
		deleteFavoriteAuthorUseCase.deleteFavoriteAuthor(user, author);

		// Then
		verify(userRepository).save(userEntity);
		List<String> expectedFavoriteAuthors = Collections.emptyList();
		assert (userEntity.getFavoriteAuthors()
				.equals(expectedFavoriteAuthors));
	}


}
