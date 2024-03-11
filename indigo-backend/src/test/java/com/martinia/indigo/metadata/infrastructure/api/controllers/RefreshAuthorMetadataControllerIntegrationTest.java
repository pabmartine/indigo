package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RefreshAuthorMetadataControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private CommandBus commandBus;

	@Test
	@WithMockUser
	public void refreshBookMetadataAuthorNotFound() throws Exception {
		// Given
		String lang = "en";
		String author = "AA. VV.";


		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/metadata/author")
				.param("lang", lang)
				.param("author", author)
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(status().isOk());
		assertEquals("", result.andReturn().getResponse().getContentAsString());
		Mockito.verify(commandBus, times(0)).executeAndWait(Mockito.any(FindAuthorMetadataCommand.class));

	}

	@Test
	@WithMockUser
	public void refreshBookMetadataAuthorOK() throws Exception {
		// Given
		String lang = "en";
		String author = "AA. VV.";


		insertAuthor();

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/metadata/author")
				.param("lang", lang)
				.param("author", author)
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("id"))
				.andExpect(jsonPath("$.name").value("AA. VV."))
				.andExpect(jsonPath("$.sort").value("AA. VV."))
				.andExpect(jsonPath("$.image").value("::image::"))
				.andExpect(jsonPath("$.numBooks").value(1));
		Mockito.verify(commandBus, times(1)).executeAndWait(Mockito.any(FindAuthorMetadataCommand.class));

	}

	private void insertAuthor() {
		final Map<String, Integer> map = new HashMap<>();
		map.put("es", 1);
		AuthorMongoEntity authorMongoEntity = AuthorMongoEntity.builder()
				.id("id")
				.name("AA. VV.")
				.sort("AA. VV.")
				.numBooks(NumBooksMongo.builder().total(1).languages(map).build())
				.image("::image::")
				.build();
		authorRepository.save(authorMongoEntity);
	}
}
