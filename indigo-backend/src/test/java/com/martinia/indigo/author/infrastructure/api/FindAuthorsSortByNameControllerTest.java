package com.martinia.indigo.author.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.infrastructure.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.mapper.AuthorDtoMapper;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.usecases.FindAuthorsSortByNameUseCase;
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

public class FindAuthorsSortByNameControllerTest extends BaseIndigoTest {

	@MockBean
	private FindAuthorsSortByNameUseCase mockUseCase;

	@MockBean
	private AuthorDtoMapper mockMapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testFindBySortName_WhenRequestParamProvided_ThenReturnOkStatusAndAuthorDto() throws Exception {
		// Given
		String sort = "name";
		Optional<Author> authorOptional = Optional.of(new Author());
		Author author = authorOptional.get();
		author.setName("John Doe");
		AuthorDto authorDto = new AuthorDto();
		authorDto.setName("John Doe");

		Mockito.when(mockUseCase.findBySort(sort)).thenReturn(authorOptional);
		Mockito.when(mockMapper.domain2Dto(author)).thenReturn(authorDto);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/author/sort").param("sort", sort).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.name").value("John Doe"));

		// Then
		Mockito.verify(mockUseCase).findBySort(sort);
		Mockito.verify(mockMapper).domain2Dto(author);
	}
}
