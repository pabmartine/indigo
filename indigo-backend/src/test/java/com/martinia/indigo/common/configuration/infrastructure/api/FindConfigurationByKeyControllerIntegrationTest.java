package com.martinia.indigo.common.configuration.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class FindConfigurationByKeyControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	private ConfigurationMongoEntity configurationMongoEntity;

	@BeforeEach
	public void init() {
		configurationMongoEntity = ConfigurationMongoEntity.builder().key("key").value("value").build();
		configurationRepository.save(configurationMongoEntity);
	}

	@Test
	public void testGetConfigurationByKey() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/config/get")
				.param("key", configurationMongoEntity.getKey())
				.contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		Configuration configuration = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(),
				new TypeReference<Configuration>() {});
		assertEquals(configurationMongoEntity.getValue(), configuration.getValue());
	}

	@Test
	public void testGetConfigurationByKeyNotFound() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/config/get").param("key", "unknown").contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
		assertEquals("", result.andReturn().getResponse().getContentAsString());
	}

}