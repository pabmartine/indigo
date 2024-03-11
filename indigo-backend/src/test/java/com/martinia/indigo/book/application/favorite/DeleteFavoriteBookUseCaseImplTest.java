package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.favorite.DeleteFavoriteBookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;

public class DeleteFavoriteBookUseCaseImplTest extends BaseIndigoTest{

	@Resource
	private DeleteFavoriteBookUseCase deleteFavoriteBookUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void testDeleteFavoriteBook_RemovesBookFromUserFavorites() {
		// Given
		String user = "john_doe";
		String book = "Book 1";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		final List<String> list = new ArrayList<>();
		list.add(book);
		userEntity.setFavoriteBooks(list);

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));

		// When
		deleteFavoriteBookUseCase.deleteFavoriteBook(user, book);

		// Then
		verify(userRepository).save(userEntity);
		List<String> expectedFavoriteBooks = new ArrayList<>();
		assert (userEntity.getFavoriteBooks().equals(expectedFavoriteBooks));
	}
}
