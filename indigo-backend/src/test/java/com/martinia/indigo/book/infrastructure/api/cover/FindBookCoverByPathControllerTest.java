package com.martinia.indigo.book.infrastructure.api.cover;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.adapters.in.rest.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.service.cover.FindBookCoverByPathUseCase;
import com.martinia.indigo.book.infrastructure.api.cover.FindBookCoverByPathController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindBookCoverByPathControllerTest extends BaseIndigoTest {
	@MockBean
	private FindBookCoverByPathUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@InjectMocks
	private FindBookCoverByPathController controller;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetImage() throws Exception {
		// Given
		String bookPath = "example_path";
		String imageUrl = "https://example.com/book/image.jpg";
		when(useCase.getImage(bookPath)).thenReturn(Optional.of(imageUrl));

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/book/image").param("path", bookPath))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.image").value(imageUrl));

		// Then
		verify(useCase, times(1)).getImage(bookPath);
	}

	@Test
	void testGetImage_NoImage() throws Exception {
		// Given
		String bookPath = "example_path";
		when(useCase.getImage(bookPath)).thenReturn(Optional.empty());

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/book/image").param("path", bookPath))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$").doesNotExist());

		// Then
		verify(useCase, times(1)).getImage(bookPath);
	}
}