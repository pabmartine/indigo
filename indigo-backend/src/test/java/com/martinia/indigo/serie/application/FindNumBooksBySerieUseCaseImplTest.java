package com.martinia.indigo.serie.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.serie.domain.ports.repositories.SerieRepository;
import com.martinia.indigo.serie.domain.ports.usecases.FindNumBooksBySerieUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class FindNumBooksBySerieUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private SerieRepository serieRepository;

	@Resource
	private FindNumBooksBySerieUseCase findNumBooksBySerieUseCase;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetNumBooksBySerie_WithValidData_ShouldReturnNumBooksBySerie() throws Exception {
		// Given
		List<String> languages = List.of("en", "es");
		int page = 1;
		int size = 10;
		String sort = "title";
		String order = "asc";
		Map<String, Long> expectedResult = new HashMap<>();
		expectedResult.put("Serie1", 5L);
		expectedResult.put("Serie2", 3L);
		when(serieRepository.getNumBooksBySerie(languages, page, size, sort, order)).thenReturn(expectedResult);

		// When
		Map<String, Long> result = findNumBooksBySerieUseCase.getNumBooksBySerie(languages, page, size, sort, order);

		// Then
		assertEquals(expectedResult, result);
	}

}
