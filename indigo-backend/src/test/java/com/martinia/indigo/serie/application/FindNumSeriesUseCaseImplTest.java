package com.martinia.indigo.serie.application;

import com.martinia.indigo.serie.domain.repository.SerieRepository;
import com.martinia.indigo.serie.domain.service.FindNumSeriesUseCase;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@AutoConfigureMockMvc
public class FindNumSeriesUseCaseImplTest {

	@MockBean
	private SerieRepository serieRepository;

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
		when(serieRepository.getNumSeries(languages)).thenReturn(expectedResult);

		// When
		long result = findNumSeriesUseCase.getNumSeries(languages);

		// Then
		assertEquals(expectedResult, result);
	}

}
