package com.martinia.indigo.common.configuration.infrastructure.api;

import static org.junit.jupiter.api.Assertions.*;

import com.martinia.indigo.adapters.in.rest.dtos.ConfigurationDto;
import com.martinia.indigo.adapters.in.rest.mappers.ConfigurationDtoMapper;
import com.martinia.indigo.common.configuration.domain.service.FindConfigurationByKeyUseCase;
import com.martinia.indigo.domain.model.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class FindConfigurationByKeyControllerTest {

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