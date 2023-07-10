package com.martinia.indigo.serie.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.serie.domain.ports.usecases.FindNumSeriesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FindNumSeriesControllerTest extends BaseIndigoTest {

	@MockBean
	private FindNumSeriesUseCase useCase;

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetNumSeries_WithValidLanguages_ShouldReturnNumSeries() throws Exception {
		// Given
		List<String> languages = List.of("en", "es");
		long numSeries = 10L;
		when(useCase.getNumSeries(anyList())).thenReturn(numSeries);

		// When
		ResultActions resultActions = mockMvc.perform(
				get("/api/serie/count").param("languages", String.join(",", languages)).contentType(MediaType.APPLICATION_JSON));
		MvcResult mvcResult = resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(numSeries)).andReturn();

	}
}
