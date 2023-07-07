package com.martinia.indigo.author.application;

import static org.junit.jupiter.api.Assertions.*;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.repository.AuthorRepository;
import com.martinia.indigo.author.domain.service.FindAllAuthorsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

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
		List<Author> expectedAuthors = Arrays.asList(
				new Author(),
				new Author()
		);
		when(authorRepository.findAll(languages, page, size, sort, order)).thenReturn(expectedAuthors);

		// When
		List<Author> result = findAllAuthorsUseCase.findAll(languages, page, size, sort, order);

		// Then
		assertEquals(expectedAuthors, result);
	}
}
