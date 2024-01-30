package com.martinia.indigo.configuration.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.usecases.SaveConfigurationsUseCase;
import com.martinia.indigo.configuration.infrastructure.api.mappers.ConfigurationDtoMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doNothing;


public class SaveConfigurationsControllerTest extends BaseIndigoTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SaveConfigurationsUseCase useCase;

	@Autowired
	private ConfigurationDtoMapper mapper;

	@Test
	@WithMockUser
	public void testSaveConfigurations() throws Exception {
		// Arrange
		Configuration configuration = new Configuration();
		List<Configuration> configurations = Collections.singletonList(configuration);
		String requestBody = (new ObjectMapper()).writeValueAsString(configurations);
		doNothing().when(useCase).save(configurations);

		// Act & Assert
		mockMvc.perform(MockMvcRequestBuilders.put("/api/config/save").contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(useCase).save(Mockito.anyList());
	}
}
