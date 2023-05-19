package com.martinia.indigo.tag.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.dtos.TagDto;
import com.martinia.indigo.adapters.in.rest.mappers.TagDtoMapper;
import com.martinia.indigo.domain.model.inner.NumBooks;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.service.FindTagByNameUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FindTagByNameControllerTest {

	@Resource
	private MockMvc mockMvc;

	@MockBean
	private FindTagByNameUseCase findTagByNameUseCase;

	@MockBean
	private TagDtoMapper tagDtoMapper;

	@Resource
	private FindTagByNameController findTagByNameController;

	@Test
	public void testGetTagByName() throws Exception {
		// Given
		String tagName = "exampleTag";

		Tag tag = new Tag("1", tagName, "image.jpg", new NumBooks());
		TagDto tagDto = new TagDto("1", tagName, "image.jpg", 0);

		when(findTagByNameUseCase.findByName(tagName)).thenReturn(Optional.of(tag));
		when(tagDtoMapper.domain2Dto(tag)).thenReturn(tagDto);

		// When
		mockMvc.perform(get("/rest/tag/tag").param("name", tagName).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(tagDto.getId())).andExpect(jsonPath("$.name").value(tagDto.getName()))
				.andExpect(jsonPath("$.image").value(tagDto.getImage())).andExpect(jsonPath("$.numBooks").value(tagDto.getNumBooks()));
	}
}
