package com.martinia.indigo.author.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.CountAllAuthorsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CountAllAuthorsUseCaseImplTest extends BaseIndigoTest {
	@Resource
	private CountAllAuthorsUseCase countAllAuthorsUseCase;

	@MockBean
	private AuthorRepository authorRepository;



	@Test
	void givenLanguages_whenCountAuthors_thenReturnCount() {
		// Given
		List<String> languages = Arrays.asList("English", "Spanish", "French");
		Long expectedCount = 42L;
		when(authorRepository.count(languages)).thenReturn(expectedCount);

		// When
		Long result = countAllAuthorsUseCase.count(languages);

		// Then
		assertEquals(expectedCount, result);
	}

	@Test
	void givenEmptyLanguages_whenCountAuthors_thenReturnZero() {
		// Given
		List<String> emptyLanguages = Arrays.asList();
		Long expectedCount = 0L;
		when(authorRepository.count(emptyLanguages)).thenReturn(expectedCount);

		// When
		Long result = countAllAuthorsUseCase.count(emptyLanguages);

		// Then
		assertEquals(expectedCount, result);
	}

	@Test
	void givenNullLanguages_whenCountAuthors_thenReturnZero() {
		// Given
		List<String> nullLanguages = null;
		Long expectedCount = 0L;
		when(authorRepository.count(nullLanguages)).thenReturn(expectedCount);

		// When
		Long result = countAllAuthorsUseCase.count(nullLanguages);

		// Then
		assertEquals(expectedCount, result);
	}

	@Test
	void givenSingleLanguage_whenCountAuthors_thenReturnCount() {
		// Given
		List<String> singleLanguage = Arrays.asList("English");
		Long expectedCount = 15L;
		when(authorRepository.count(singleLanguage)).thenReturn(expectedCount);

		// When
		Long result = countAllAuthorsUseCase.count(singleLanguage);

		// Then
		assertEquals(expectedCount, result);
	}
}
