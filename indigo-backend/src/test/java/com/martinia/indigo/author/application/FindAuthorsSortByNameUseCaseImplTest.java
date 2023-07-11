package com.martinia.indigo.author.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorMongoRepository;
import com.martinia.indigo.author.domain.ports.usecases.FindAuthorsSortByNameUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FindAuthorsSortByNameUseCaseImplTest extends BaseIndigoTest {
	@Resource
	private FindAuthorsSortByNameUseCase findAuthorsSortByNameUseCase;

	@MockBean
	private AuthorMongoRepository authorMongoRepository;

	@Test
	void givenName_whenFindBySort_thenReturnAuthor() {
		// Given
		String name = "John Doe";
		AuthorMongoEntity expectedAuthor = new AuthorMongoEntity();
		when(authorMongoRepository.findBySort(name)).thenReturn(Optional.of(expectedAuthor));

		// When
		Optional<Author> result = findAuthorsSortByNameUseCase.findBySort(name);

		// Then
		assertEquals(Optional.of(expectedAuthor), result);
	}

	@Test
	void givenName_whenFindBySort_thenReturnEmptyOptional() {
		// Given
		String name = "Unknown Author";
		when(authorMongoRepository.findBySort(name)).thenReturn(Optional.empty());

		// When
		Optional<Author> result = findAuthorsSortByNameUseCase.findBySort(name);

		// Then
		assertEquals(Optional.empty(), result);
	}
}
