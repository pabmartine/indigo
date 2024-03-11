package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class StopMetadataControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Test
	@WithMockUser
	public void testStopMetadata() throws Exception {

		//Given
		metadataSingleton.start("type", "entity");
		metadataSingleton.setCurrent(10);
		metadataSingleton.setTotal(100);
		metadataSingleton.setMessage("message");
		// When
		ResultActions resultActions = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/metadata/stop").contentType(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(MockMvcResultMatchers.status().isOk());
		assertNull(metadataSingleton.getType());
		assertFalse(metadataSingleton.isRunning());
		assertEquals(0, metadataSingleton.getTotal());
		assertEquals(0, metadataSingleton.getCurrent());
		assertNull(metadataSingleton.getMessage());

	}
}
