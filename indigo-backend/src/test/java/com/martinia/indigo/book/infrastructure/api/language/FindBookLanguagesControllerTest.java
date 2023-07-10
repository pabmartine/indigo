package com.martinia.indigo.book.infrastructure.api.language;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.language.FindBookLanguagesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindBookLanguagesControllerTest extends BaseIndigoTest {

	@MockBean
	private FindBookLanguagesUseCase useCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetBookLanguages() throws Exception {
		// Given
		List<String> languages = Arrays.asList("English", "Spanish", "French");
		when(useCase.getBookLanguages()).thenReturn(languages);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/book/languages")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("English"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]").value("Spanish"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[2]").value("French"));

		// Then
		verify(useCase, times(1)).getBookLanguages();
	}
}