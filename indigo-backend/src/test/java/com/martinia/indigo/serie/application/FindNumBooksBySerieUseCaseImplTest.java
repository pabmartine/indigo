package com.martinia.indigo.serie.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.serie.domain.ports.usecases.FindNumBooksBySerieUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindNumBooksBySerieUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindNumBooksBySerieUseCase findNumBooksBySerieUseCase;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void testGetNumBooksBySerie_ReturnsNumBooksBySerie() {
		// Given
		List<String> languages = Arrays.asList("English", "Spanish");
		int page = 0;
		int size = 10;
		String sort = "name";
		String order = "asc";

		Map<String, Long> expectedNumBooksBySerie = new HashMap<>();
		expectedNumBooksBySerie.put("Serie 1", 10L);
		expectedNumBooksBySerie.put("Serie 2", 5L);

		when(bookRepository.getNumBooksBySerie(languages, page, size, sort, order)).thenReturn(expectedNumBooksBySerie);

		// When
		Map<String, Long> result = findNumBooksBySerieUseCase.getNumBooksBySerie(languages, page, size, sort, order);

		// Then
		assertEquals(expectedNumBooksBySerie, result);
	}
}
