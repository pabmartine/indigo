package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.favorite.FindFavoriteAuthorsUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FindFavoriteAuthorsUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindFavoriteAuthorsUseCase findFavoriteAuthorsUseCase;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private AuthorRepository authorRepository;

	@MockBean
	private AuthorMongoMapper authorMongoMapper;

	@Test
	public void testGetFavoriteAuthors_ReturnsEmptyListWhenUserHasNoFavoriteAuthors() {
		// Given
		String user = "john_doe";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		userEntity.setFavoriteAuthors(Collections.emptyList());

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));

		// When
		List<Author> favoriteAuthors = findFavoriteAuthorsUseCase.getFavoriteAuthors(user);

		// Then
		verify(userRepository).findByUsername(user);
		assert (favoriteAuthors.isEmpty());
	}

	@Test
	public void testGetFavoriteAuthors_ReturnsAuthorsWhenUserHasFavoriteAuthors() {
		// Given
		String user = "john_doe";
		List<String> favoriteAuthorNames = Arrays.asList("Jane Smith", "John Doe");

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		userEntity.setFavoriteAuthors(favoriteAuthorNames);

		AuthorMongoEntity author1 = new AuthorMongoEntity();
		author1.setId("1");
		author1.setName("Jane Smith");

		AuthorMongoEntity author2 = new AuthorMongoEntity();
		author2.setId("2");
		author2.setName("John Doe");

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));
		when(authorRepository.findBySort("Jane Smith")).thenReturn(Optional.of(author1));
		when(authorRepository.findBySort("John Doe")).thenReturn(Optional.of(author2));
		when(authorMongoMapper.entity2Domain(author1)).thenReturn(new Author());
		when(authorMongoMapper.entity2Domain(author2)).thenReturn(new Author());

		// When
		List<Author> favoriteAuthors = findFavoriteAuthorsUseCase.getFavoriteAuthors(user);

		// Then
		verify(userRepository).findByUsername(user);
		verify(authorRepository).findBySort("Jane Smith");
		verify(authorRepository).findBySort("John Doe");
		verify(authorMongoMapper).entity2Domain(author1);
		verify(authorMongoMapper).entity2Domain(author2);

		assert (favoriteAuthors.size() == favoriteAuthorNames.size());
	}
}
