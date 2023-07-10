package com.martinia.indigo.book.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByIdUseCase;
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

class FindBookByIdControllerTest extends BaseIndigoTest {
	@MockBean
	private FindBookByIdUseCase useCase;

	@MockBean
	private BookDtoMapper mapper;

	@InjectMocks
	private FindBookByIdController controller;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testGetBookById() throws Exception {
		// Given
		String bookId = "example_id";
		BookDto bookDto = new BookDto();
		bookDto.setId(bookId);
		when(useCase.findById(any())).thenReturn(Optional.of(new Book()));
		when(mapper.domain2Dto(any())).thenReturn(bookDto);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/book/id").param("id", bookId)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().json("{\"id\":\"example_id\"}"));

		// Then
		verify(useCase, times(1)).findById(any());
		verify(mapper, times(1)).domain2Dto(any());
	}
}