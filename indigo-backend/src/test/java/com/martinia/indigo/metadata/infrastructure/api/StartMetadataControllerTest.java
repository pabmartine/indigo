package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.service.StartMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

public class StartMetadataControllerTest extends BaseIndigoTest {

	@MockBean
	private StartMetadataUseCase mockUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testInitialLoad_WhenRequestParamsProvided_ThenReturnOkStatus() throws Exception {
		// Given
		String lang = "en";
		String type = "book";
		String entity = "exampleEntity";

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/metadata/start").param("lang", lang).param("type", type).param("entity", entity)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		Mockito.verify(mockUseCase).start(lang, type, entity);
	}
}
