package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.infrastructure.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.mapper.AuthorDtoMapper;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshAuthorMetadataUseCase;
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

public class RefreshAuthorMetadataControllerTest extends BaseIndigoTest {

	@MockBean
	private RefreshAuthorMetadataUseCase mockUseCase;

	@MockBean
	private AuthorDtoMapper mockMapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testRefreshAuthor_WhenRequestParamsProvided_ThenReturnOkStatusAndAuthorDto() throws Exception {
		// Given
		String lang = "en";
		String author = "John Doe";
		Optional<Author> authorOptional = Optional.of(new Author());
		Author _author = authorOptional.get();
		_author.setName("John Doe");
		AuthorDto authorDto = new AuthorDto();
		authorDto.setName("John Doe");

		Mockito.when(mockUseCase.findAuthorMetadata(author, lang)).thenReturn(authorOptional);
		Mockito.when(mockMapper.domain2Dto(_author)).thenReturn(authorDto);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/metadata/author").param("lang", lang).param("author", author)
						.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value("John Doe"));

		// Then
		Mockito.verify(mockUseCase).findAuthorMetadata(author, lang);
		Mockito.verify(mockMapper).domain2Dto(_author);
	}
}
