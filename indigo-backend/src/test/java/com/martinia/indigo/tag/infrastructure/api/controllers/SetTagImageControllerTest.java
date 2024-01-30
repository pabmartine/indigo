package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.ports.usecases.SetTagImageUseCase;
import com.martinia.indigo.tag.infrastructure.api.controllers.SetTagImageController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SetTagImageControllerTest extends BaseIndigoTest {

	private static final String BASE_PATH = "/api/tag";

	@MockBean
	private SetTagImageUseCase setTagImageUseCase;

	@Resource
	private SetTagImageController setTagImageController;

	@Resource
	private MockMvc mockMvc;

	@Test
	public void testSetImage() throws Exception {
		// Given
		String source = "sourceTag";
		String image = "tagImage";

		// Mock the behavior of the use case
		doNothing().when(setTagImageUseCase).setImage(source, image);

		// When
		mockMvc.perform(get(BASE_PATH + "/image").param("source", source).param("image", image).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().string(""));
	}
}
