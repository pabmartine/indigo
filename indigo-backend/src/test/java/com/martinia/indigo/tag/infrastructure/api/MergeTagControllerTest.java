package com.martinia.indigo.tag.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.ports.usecases.MergeTagUseCase;
import com.martinia.indigo.tag.infrastructure.api.controllers.MergeTagController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class MergeTagControllerTest extends BaseIndigoTest {

	@Resource
	private MockMvc mockMvc;

	@MockBean
	private MergeTagUseCase mergeTagUseCase;

	@Resource
	private MergeTagController mergeTagController;

	@Test
	@Transactional
	public void testMerge() throws Exception {
		// Given
		String source = "sourceTag";
		String target = "targetTag";

		// Mock the behavior of the use case
		doNothing().when(mergeTagUseCase).merge(source, target);

		// When
		mockMvc.perform(get("/api/tag/merge").param("source", source).param("target", target).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().string(""));
	}
}
