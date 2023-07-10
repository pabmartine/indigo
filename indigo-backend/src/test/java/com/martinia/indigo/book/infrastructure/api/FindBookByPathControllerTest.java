package com.martinia.indigo.book.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByPathUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindBookByPathControllerTest extends BaseIndigoTest {
	@MockBean
	private FindBookByPathUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@InjectMocks
	private FindBookByPathController controller;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetBookByPath() throws Exception {
		// Given
		String bookPath = "example_path";
		BookDto bookDto = new BookDto();
		bookDto.setPath(bookPath);
		when(useCase.findByPath(bookPath)).thenReturn(Optional.of(new Book()));
		when(mapper.domain2Dto(any())).thenReturn(bookDto);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/book/path").param("path", bookPath))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().json("{\"path\":\"example_path\"}"));

		// Then
		verify(useCase, times(1)).findByPath(bookPath);
		verify(mapper, times(1)).domain2Dto(any());
	}
}