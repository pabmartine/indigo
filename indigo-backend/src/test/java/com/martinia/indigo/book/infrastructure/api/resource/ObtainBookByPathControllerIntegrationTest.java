package com.martinia.indigo.book.infrastructure.api.resource;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.common.util.UtilComponent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;

class ObtainBookByPathControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private UtilComponent utilComponent;

	@Test
	void testGetEpub() throws Exception {
		// Given
		org.springframework.core.io.Resource resource = Mockito.mock(org.springframework.core.io.Resource.class);
		File file = new File("test.json");
		Mockito.when(resource.getFile()).thenReturn(file);
		Mockito.when(resource.getFilename()).thenReturn("file");
		Mockito.when(utilComponent.getEpub(Mockito.anyString())).thenReturn(resource);

		final String path = "path";

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/epub").param("path", path));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isInternalServerError());

		file.delete();
	}

}