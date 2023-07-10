package com.martinia.indigo.common.configuration.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.configuration.infrastructure.model.ConfigurationDto;
import com.martinia.indigo.configuration.infrastructure.mapper.ConfigurationDtoMapper;
import com.martinia.indigo.configuration.domain.ports.usecases.FindConfigurationByKeyUseCase;
import com.martinia.indigo.configuration.domain.model.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.when;


public class FindConfigurationByKeyControllerTest extends BaseIndigoTest {

	@Resource
	private MockMvc mockMvc;

	@MockBean
	private FindConfigurationByKeyUseCase useCase;

	@Resource
	private ConfigurationDtoMapper mapper;

	@Test
	public void testGetConfigurationByKey() throws Exception {
		// Arrange
		String key = "testKey";
		Configuration configuration = new Configuration();
		configuration.setKey(key);
		ConfigurationDto expectedDto = mapper.domain2Dto(configuration);
		when(useCase.findByKey(key)).thenReturn(Optional.of(configuration));

		// Act & Assert
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/config/get").param("key", key).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.key").value(expectedDto.getKey()));
	}
}