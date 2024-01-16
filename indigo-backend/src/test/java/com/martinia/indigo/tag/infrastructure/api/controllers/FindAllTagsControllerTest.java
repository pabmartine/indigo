package com.martinia.indigo.tag.infrastructure.api.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.infrastructure.api.controllers.FindAllTagsController;
import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
import com.martinia.indigo.tag.infrastructure.api.mappers.TagDtoMapper;
import com.martinia.indigo.common.model.NumBooks;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.usecases.FindAllTagsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.Resource;


public class FindAllTagsControllerTest extends BaseIndigoTest {


	@Resource
	private MockMvc mockMvc;

	@MockBean
	private FindAllTagsUseCase findAllTagsUseCase;

	@MockBean
	private TagDtoMapper tagDtoMapper;

	@Resource
	private FindAllTagsController findAllTagsController;

	@BeforeEach
	public void setup() {
		// Configure MockMvc standalone setup
		mockMvc = MockMvcBuilders.standaloneSetup(findAllTagsController).build();
	}

	@Test
	public void testGetAllTags() throws Exception {
		// Given
		List<String> languages = Arrays.asList("en", "es");
		String sort = "name";
		String order = "asc";

		Tag tag1 = new Tag("1", "Tag 1", "image1.jpg", new NumBooks());
		Tag tag2 = new Tag("2", "Tag 2", "image2.jpg", new NumBooks());
		List<Tag> tags = Arrays.asList(tag1, tag2);

		TagDto tagDto1 = new TagDto("1", "Tag 1", "image1.jpg", 0);
		TagDto tagDto2 = new TagDto("2", "Tag 2", "image2.jpg", 0);
		List<TagDto> tagsDto = Arrays.asList(tagDto1, tagDto2);

		when(findAllTagsUseCase.findAll(languages, sort, order)).thenReturn(tags);
		when(tagDtoMapper.domains2Dtos(tags)).thenReturn(tagsDto);

		// When
		mockMvc.perform(get("/api/tag/all").param("languages", "en", "es").param("sort", sort).param("order", order)
						.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(tagsDto.size()))
				.andExpect(jsonPath("$[0].id").value(tagDto1.getId())).andExpect(jsonPath("$[0].name").value(tagDto1.getName()))
				.andExpect(jsonPath("$[0].image").value(tagDto1.getImage()))
				.andExpect(jsonPath("$[0].numBooks").value(tagDto1.getNumBooks())).andExpect(jsonPath("$[1].id").value(tagDto2.getId()))
				.andExpect(jsonPath("$[1].name").value(tagDto2.getName())).andExpect(jsonPath("$[1].image").value(tagDto2.getImage()))
				.andExpect(jsonPath("$[1].numBooks").value(tagDto2.getNumBooks()));
	}
}
