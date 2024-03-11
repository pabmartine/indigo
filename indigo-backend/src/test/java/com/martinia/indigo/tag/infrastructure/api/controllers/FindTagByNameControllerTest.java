package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.infrastructure.api.controllers.FindTagByNameController;
import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
import com.martinia.indigo.tag.infrastructure.api.mappers.TagDtoMapper;
import com.martinia.indigo.common.domain.model.NumBooks;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.usecases.FindTagByNameUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FindTagByNameControllerTest extends BaseIndigoTest {

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
		mockMvc.perform(get("/api/tag/tag").param("name", tagName).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(tagDto.getId())).andExpect(jsonPath("$.name").value(tagDto.getName()))
				.andExpect(jsonPath("$.image").value(tagDto.getImage())).andExpect(jsonPath("$.numBooks").value(tagDto.getNumBooks()));
	}
}
