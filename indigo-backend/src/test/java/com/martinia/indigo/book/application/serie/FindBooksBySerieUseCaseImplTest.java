package com.martinia.indigo.book.application.serie;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.serie.FindBooksBySerieUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class FindBooksBySerieUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private BookRepository bookRepository;
	@Resource
	private FindBooksBySerieUseCase findBooksBySerieUseCase;

	@Test
	public void testGetSerie() {
		// Given
		String serie = "exampleSerie";
		List<String> languages = Arrays.asList("English", "Spanish");
		List<Book> expectedBooks = Arrays.asList(new Book(), new Book());

		// Mock the behavior of bookRepository.getSerie()
		when(bookRepository.getSerie(serie, languages)).thenReturn(expectedBooks);

		// When
		List<Book> actualBooks = findBooksBySerieUseCase.getSerie(serie, languages);

		// Then
		assertNotNull(actualBooks);
		assertEquals(expectedBooks.size(), actualBooks.size());
		assertTrue(actualBooks.containsAll(expectedBooks));
	}
}