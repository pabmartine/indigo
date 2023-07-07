package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.service.StopMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

public class StopMetadataControllerTest extends BaseIndigoTest {

	@MockBean
	private StopMetadataUseCase mockUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testStop_ThenReturnOkStatus() throws Exception {
		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/metadata/stop").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		Mockito.verify(mockUseCase).stop();
	}
}
