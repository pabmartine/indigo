package com.martinia.indigo.book.infrastructure.api.resource;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.service.resource.ObtainBookByPathUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObtainBookByPathControllerTest extends BaseIndigoTest {

	@MockBean
	private ObtainBookByPathUseCase useCase;

	@javax.annotation.Resource
	private MockMvc mockMvc;

	@Test
	void testGetEpub() throws Exception {
		// Given
		String path = "example_path";
		Resource epubResource = Mockito.mock(Resource.class);
		File file = Mockito.mock(File.class);
		when(file.toPath()).thenReturn(Mockito.mock(Path.class));
		when(epubResource.getFile()).thenReturn(file);
		when(epubResource.getFilename()).thenReturn(path);
		when(useCase.getEpub(any())).thenReturn(epubResource);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/book/epub").param("path", path))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());

		// Then
		verify(useCase, times(1)).getEpub(path);
	}

}