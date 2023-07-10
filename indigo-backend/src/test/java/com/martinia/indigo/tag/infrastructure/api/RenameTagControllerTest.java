package com.martinia.indigo.tag.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.ports.usecases.RenameTagUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class RenameTagControllerTest extends BaseIndigoTest {

	private static final String BASE_PATH = "/rest/tag";

	@MockBean
	private RenameTagUseCase renameTagUseCase;

	@Resource
	private RenameTagController renameTagController;

	@Resource
	private MockMvc mockMvc;

	@Test
	public void testRename() throws Exception {
		// Given
		String source = "sourceTag";
		String target = "targetTag";

		// Mock the behavior of the use case
		doNothing().when(renameTagUseCase).rename(source, target);

		// When
		mockMvc.perform(get(BASE_PATH + "/rename").param("source", source).param("target", target).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().string(""));
	}
}
