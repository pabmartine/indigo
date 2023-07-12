package com.martinia.indigo.serie.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.serie.domain.ports.usecases.FindNumSeriesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FindNumSeriesUseCaseImplTest extends BaseIndigoTest {

	//	@MockBean
	//	private SerieRepository serieRepository;

	@Resource
	private FindNumSeriesUseCase findNumSeriesUseCase;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetNumSeries_WithValidData_ShouldReturnNumSeries() throws Exception {
		// Given
		List<String> languages = List.of("en", "es");
		long expectedResult = 10L;
		//		when(serieRepository.getNumSeries(languages)).thenReturn(expectedResult);

		// When
		long result = findNumSeriesUseCase.getNumSeries(languages);

		// Then
		assertEquals(expectedResult, result);
	}

}
