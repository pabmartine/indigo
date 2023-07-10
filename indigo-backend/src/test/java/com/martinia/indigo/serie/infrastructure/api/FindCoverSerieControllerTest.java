package com.martinia.indigo.serie.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FindCoverSerieControllerTest  extends BaseIndigoTest {

	@MockBean
	private FindCoverSerieUseCase useCase;

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetCover_WithValidSerie_ShouldReturnCoverImage() throws Exception {
		// Given
		String serie = "TestSerie";
		String coverImage = "cover.jpg";
		when(useCase.getCover(anyString())).thenReturn(coverImage);

		// When
		ResultActions resultActions = mockMvc.perform(
				get("/rest/serie/cover").param("serie", serie).contentType(MediaType.APPLICATION_JSON));
		MvcResult mvcResult = resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.image").value(coverImage)).andReturn();

	}
}
