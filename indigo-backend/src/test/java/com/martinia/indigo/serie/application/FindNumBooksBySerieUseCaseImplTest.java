package com.martinia.indigo.serie.application;

import com.martinia.indigo.serie.domain.repository.SerieRepository;
import com.martinia.indigo.serie.domain.service.FindNumBooksBySerieUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@AutoConfigureMockMvc
public class FindNumBooksBySerieUseCaseImplTest {

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
