package com.martinia.indigo.author.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.FindAllAuthorsUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FindAllAuthorsUseCaseImplTest extends BaseIndigoTest {
	@Resource
	private FindAllAuthorsUseCase findAllAuthorsUseCase;

	@MockBean
	private AuthorRepository authorRepository;

	@Test
	void givenLanguagesPageAndSize_whenFindAllAuthors_thenReturnListOfAuthors() {
		// Given
		List<String> languages = Arrays.asList("English", "Spanish");
		int page = 0;
		int size = 10;
		String sort = "name";
		String order = "asc";
		List<AuthorMongoEntity> expectedAuthors = Arrays.asList(new AuthorMongoEntity(), new AuthorMongoEntity());
		when(authorRepository.findAll(languages, PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort)))).thenReturn(expectedAuthors);

		// When
		List<Author> result = findAllAuthorsUseCase.findAll(languages, page, size, sort, order);

		// Then
		assertEquals(expectedAuthors, result);
	}
}
