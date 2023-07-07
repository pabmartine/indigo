package com.martinia.indigo.author.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.repository.AuthorRepository;
import com.martinia.indigo.author.domain.service.FindAuthorsSortByNameUseCase;
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
	private AuthorRepository authorRepository;

	@Test
	void givenName_whenFindBySort_thenReturnAuthor() {
		// Given
		String name = "John Doe";
		Author expectedAuthor = new Author();
		when(authorRepository.findBySort(name)).thenReturn(Optional.of(expectedAuthor));

		// When
		Optional<Author> result = findAuthorsSortByNameUseCase.findBySort(name);

		// Then
		assertEquals(Optional.of(expectedAuthor), result);
	}

	@Test
	void givenName_whenFindBySort_thenReturnEmptyOptional() {
		// Given
		String name = "Unknown Author";
		when(authorRepository.findBySort(name)).thenReturn(Optional.empty());

		// When
		Optional<Author> result = findAuthorsSortByNameUseCase.findBySort(name);

		// Then
		assertEquals(Optional.empty(), result);
	}
}
