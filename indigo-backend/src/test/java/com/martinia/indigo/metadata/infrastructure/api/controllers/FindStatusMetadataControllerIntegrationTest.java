package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.ports.usecases.FindStatusMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class FindStatusMetadataControllerIntegrationTest extends BaseIndigoIntegrationTest
{

	@Resource
	private MetadataSingleton metadataSingleton;


	@Test
	@WithMockUser
	public void findStatus() throws Exception {
		// Given
		metadataSingleton.setType("type");
		metadataSingleton.setEntity("entity");
		metadataSingleton.setCurrent(1);
		metadataSingleton.setTotal(2);
		metadataSingleton.setMessage("message");


		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/metadata/status").contentType(MediaType.APPLICATION_JSON));
				

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.type").value(metadataSingleton.getType()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.entity").value(metadataSingleton.getEntity()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.current").value(metadataSingleton.getCurrent()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.total").value(metadataSingleton.getTotal()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(metadataSingleton.getMessage()));
	}
}
