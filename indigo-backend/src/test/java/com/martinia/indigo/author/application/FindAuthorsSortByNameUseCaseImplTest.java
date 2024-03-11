package com.martinia.indigo.author.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.FindAuthorsSortByNameUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class FindAuthorsSortByNameUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindAuthorsSortByNameUseCase findAuthorsSortByNameUseCase;

	@MockBean
	private AuthorRepository authorRepository;

	@MockBean
	private AuthorMongoMapper authorMongoMapper;

	@Test
	public void testFindBySort_WithExistingSort_ReturnsAuthor() {
		// Given
		String sort = "name";

		AuthorMongoEntity authorEntity = new AuthorMongoEntity();
		authorEntity.setId("1");
		authorEntity.setName("John Doe");

		Author author = new Author();
		author.setId("1");
		author.setName("John Doe");

		when(authorRepository.findByName(eq(sort))).thenReturn(Optional.of(authorEntity));
		when(authorMongoMapper.entity2Domain(authorEntity)).thenReturn(author);

		// When
		Optional<Author> result = findAuthorsSortByNameUseCase.findBySort(sort);

		// Then
		assertEquals(Optional.of(author), result);
	}

	@Test
	public void testFindBySort_WithNonExistingSort_ReturnsEmptyOptional() {
		// Given
		String sort = "non-existing-sort";

		when(authorRepository.findByName(eq(sort))).thenReturn(Optional.empty());

		// When
		Optional<Author> result = findAuthorsSortByNameUseCase.findBySort(sort);

		// Then
		assertEquals(Optional.empty(), result);
	}
}
