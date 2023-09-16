package com.martinia.indigo.book.infrastructure.api.cover;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.util.UtilComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FindBookCoverByPathControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	@MockBean
	private UtilComponent utilComponent;

	private BookMongoEntity bookMongoEntity;

	@BeforeEach
	public void init() {
		bookMongoEntity = BookMongoEntity.builder().id(UUID.randomUUID().toString()).title("title").image("image").path("path").build();
		bookRepository.save(bookMongoEntity);
	}

	@Test
	void testGetImage() throws Exception {
		// Given
		Mockito.when(utilComponent.getBase64Cover(anyString(), anyBoolean())).thenReturn("image");

		// When
		ResultActions resultActions = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/book/image").param("path", bookMongoEntity.getPath()));

		// Then
		resultActions.andExpect(status().isOk());
		resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.image").value(bookMongoEntity.getImage()));
	}

	@Test
	void testGetImageNoPath() throws Exception {
		// Given
		final String path = "unknown";
		Mockito.when(utilComponent.getBase64Cover(anyString(), anyBoolean())).thenReturn(null);

		// When
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/book/image").param("path", path));

		// Then
		resultActions.andExpect(status().isOk());
		assertEquals("", resultActions.andReturn().getResponse().getContentAsString());
	}
}