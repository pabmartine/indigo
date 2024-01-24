package com.martinia.indigo.common.configuration.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SaveConfigurationsControllerIntegrationTest extends BaseIndigoTest {

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testSaveConfigurations() throws Exception {
		// Given
		Configuration configuration = new Configuration();
		configuration.setKey("key");
		configuration.setValue("value");
		List<Configuration> configurations = Collections.singletonList(configuration);
		String requestBody = (new ObjectMapper()).writeValueAsString(configurations);

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.put("/api/config/save").contentType(MediaType.APPLICATION_JSON).content(requestBody));

		//Then

		result.andExpect(MockMvcResultMatchers.status().isOk());

		assertEquals("value", configurationRepository.findByKey("key").get().getValue());
	}

	@Test
	@WithMockUser
	public void testSaveConfigurationsExisting() throws Exception {
		// Given

		configurationRepository.save(ConfigurationMongoEntity.builder().key("key").value("value").build());

		Configuration configuration = new Configuration();
		configuration.setKey("key");
		configuration.setValue("value2");

		List<Configuration> configurations = Collections.singletonList(configuration);
		String requestBody = (new ObjectMapper()).writeValueAsString(configurations);

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.put("/api/config/save").contentType(MediaType.APPLICATION_JSON).content(requestBody));

		//Then

		result.andExpect(MockMvcResultMatchers.status().isOk());

		assertEquals("value2", configurationRepository.findByKey("key").get().getValue());
	}
}
