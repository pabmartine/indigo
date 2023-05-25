package com.martinia.indigo.common.configuration.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.adapters.in.rest.mappers.ConfigurationDtoMapper;
import com.martinia.indigo.common.configuration.domain.service.SaveConfigurationsUseCase;
import com.martinia.indigo.common.configuration.domain.model.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doNothing;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class SaveConfigurationsControllerTest {

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
		mockMvc.perform(MockMvcRequestBuilders.put("/rest/config/save").contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(useCase).save(Mockito.anyList());
	}
}
