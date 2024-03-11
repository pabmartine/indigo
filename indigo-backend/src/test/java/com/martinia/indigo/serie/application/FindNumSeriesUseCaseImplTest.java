package com.martinia.indigo.serie.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.serie.domain.ports.usecases.FindNumSeriesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindNumSeriesUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindNumSeriesUseCase findNumSeriesUseCase;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void testGetNumSeries_ReturnsNumSeries() {
		// Given
		List<String> languages = Arrays.asList("English", "Spanish");
		long expectedNumSeries = 5;

		when(bookRepository.getNumSeries(languages)).thenReturn(expectedNumSeries);

		// When
		long result = findNumSeriesUseCase.getNumSeries(languages);

		// Then
		assertEquals(expectedNumSeries, result);
	}
}
