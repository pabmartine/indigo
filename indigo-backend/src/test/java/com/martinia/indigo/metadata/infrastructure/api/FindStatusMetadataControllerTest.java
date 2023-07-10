package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.usecases.FindStatusMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class FindStatusMetadataControllerTest extends BaseIndigoTest {

	@MockBean
	private FindStatusMetadataUseCase mockUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testGetStatus_ThenReturnOkStatusAndMetadata() throws Exception {
		// Given
		Map<String, Object> statusMetadata = new HashMap<>();
		statusMetadata.put("status", "OK");
		statusMetadata.put("version", "1.0");

		Mockito.when(mockUseCase.getStatus()).thenReturn(statusMetadata);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/api/metadata/status").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.version").value("1.0"));

		// Then
		Mockito.verify(mockUseCase).getStatus();
	}
}
