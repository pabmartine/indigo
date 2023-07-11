package com.martinia.indigo.book.application.language;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.language.FindBookLanguagesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class FindBookLanguagesUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookLanguagesUseCase findBookLanguagesUseCase;

	@MockBean
	private BookMongoRepository bookRepository;

	@Test
	public void testGetBookLanguages() {
		// Given
		List<String> expectedLanguages = Arrays.asList("English", "Spanish", "French");

		// Mock the behavior of bookRepository.getBookLanguages()
		when(bookRepository.getBookLanguages()).thenReturn(expectedLanguages);

		// When
		List<String> actualLanguages = findBookLanguagesUseCase.getBookLanguages();

		// Then
		assertNotNull(actualLanguages);
		assertEquals(expectedLanguages.size(), actualLanguages.size());
		assertTrue(actualLanguages.containsAll(expectedLanguages));
	}
}
