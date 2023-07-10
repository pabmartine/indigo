package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshBookMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Optional;

public class RefreshBookMetadataControllerTest extends BaseIndigoTest {

	@MockBean
	private RefreshBookMetadataUseCase mockUseCase;

	@MockBean
	private BookDtoMapper mockMapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testRefreshBook_WhenRequestParamProvided_ThenReturnOkStatusAndBookDto() throws Exception {
		// Given
		String book = "exampleBook";
		Optional<Book> bookOptional = Optional.of(new Book());
		Book _book = bookOptional.get();
		_book.setTitle("Example Book");
		BookDto bookDto = new BookDto();
		bookDto.setTitle("Example Book");

		Mockito.when(mockUseCase.findBookMetadata(book)).thenReturn(bookOptional);
		Mockito.when(mockMapper.domain2Dto(_book)).thenReturn(bookDto);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/metadata/book").param("book", book).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Example Book"));

		// Then
		Mockito.verify(mockUseCase).findBookMetadata(book);
		Mockito.verify(mockMapper).domain2Dto(_book);
	}
}
