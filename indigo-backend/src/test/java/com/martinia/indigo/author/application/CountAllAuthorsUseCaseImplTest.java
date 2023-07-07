package com.martinia.indigo.author.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.repository.AuthorRepository;
import com.martinia.indigo.author.domain.service.CountAllAuthorsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
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
}
