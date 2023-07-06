package com.martinia.indigo.book.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.service.CountAllBooksUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountAllBooksControllerTest extends BaseIndigoTest {

	@MockBean
	private CountAllBooksUseCase useCase;

	@InjectMocks
	private CountAllBooksController controller;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	void testGetTotalAdvSearch() throws Exception {
		// Given
		when(useCase.count(any())).thenReturn(10L);

		// When
		mockMvc.perform(MockMvcRequestBuilders.post("/rest/book/count/search/advance").contentType(MediaType.APPLICATION_JSON_VALUE)
						.content("{\"query\":\"example\"}")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$").value(10));

		// Then
		verify(useCase, times(1)).count(any());
	}
}
