package com.martinia.indigo.author.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.CountAllAuthorsUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

public class CountAllAuthorsControllerTest extends BaseIndigoTest {

	@MockBean
	private CountAllAuthorsUseCase mockUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testCount_WhenRequestParamProvided_ThenReturnOkStatusAndCount() throws Exception {
		// Given
		List<String> languages = Arrays.asList("English", "Spanish");
		long count = 10L;
		Mockito.when(mockUseCase.count(languages)).thenReturn(count);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/author/count").param("languages", "English", "Spanish")
						.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(String.valueOf(count)));

		// Then
		Mockito.verify(mockUseCase).count(languages);
	}
}
