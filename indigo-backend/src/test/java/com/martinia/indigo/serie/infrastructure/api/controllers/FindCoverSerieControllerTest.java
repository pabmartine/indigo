package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
		byte[] coverImage = "cover".getBytes();
		when(useCase.getCover(anyString())).thenReturn(coverImage);

		// When
		mockMvc.perform(get("/api/serie/cover").param("serie", serie))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG))
				.andExpect(content().bytes(coverImage));
	}

	@Test
	public void testGetCover_NotFound() throws Exception {
		when(useCase.getCover(anyString())).thenReturn(new byte[0]);

		mockMvc.perform(get("/api/serie/cover").param("serie", "Unknown"))
				.andExpect(status().isNotFound());
	}
}
