package com.martinia.indigo.author.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.infrastructure.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.mapper.AuthorDtoMapper;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.usecases.FindAllAuthorsUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class FindAllAuthorsControllerTest extends BaseIndigoTest {

	@MockBean
	private FindAllAuthorsUseCase mockUseCase;

	@MockBean
	private AuthorDtoMapper mockMapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testGetAll_WhenRequestParamsProvided_ThenReturnOkStatusAndAuthorDtoList() throws Exception {
		// Given
		List<String> languages = new ArrayList<>();
		languages.add("English");
		languages.add("Spanish");
		int page = 1;
		int size = 10;
		String sort = "name";
		String order = "asc";

		List<Author> authors = new ArrayList<>();
		Author author1 = new Author();
		author1.setName("John Doe");
		authors.add(author1);
		Author author2 = new Author();
		author2.setName("Jane Smith");
		authors.add(author2);

		List<AuthorDto> authorsDto = new ArrayList<>();
		AuthorDto authorDto1 = new AuthorDto();
		authorDto1.setName("John Doe");
		authorsDto.add(authorDto1);
		AuthorDto authorDto2 = new AuthorDto();
		authorDto2.setName("Jane Smith");
		authorsDto.add(authorDto2);

		Mockito.when(mockUseCase.findAll(languages, page, size, sort, order)).thenReturn(authors);
		Mockito.when(mockMapper.domains2Dtos(authors)).thenReturn(authorsDto);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/author/all").param("languages", "English", "Spanish").param("page", "1")
						.param("size", "10").param("sort", "name").param("order", "asc").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("John Doe"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Jane Smith"));

		// Then
		Mockito.verify(mockUseCase).findAll(languages, page, size, sort, order);
		Mockito.verify(mockMapper).domains2Dtos(authors);
	}
}
