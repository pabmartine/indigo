package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.FindReviewMetadataCommand;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RefreshBookMetadataControllerIntegrationTest extends BaseIndigoIntegrationTest {

	public static final String PATH = "path";
	public static final String TITLE = "title";

	@MockBean
	private CommandBus commandBus;

	@Test
	public void refreshBookMetadataBookNotFound() throws Exception {
		// Given

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/metadata/book")
				.param("book", "book_path")
				.param("lang", "es")
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(status().isOk());
		assertEquals("", result.andReturn().getResponse().getContentAsString());
	}

	@Test
	public void refreshBookMetadataBookOk() throws Exception {
		// Given
		insertBook();

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/metadata/book")
				.param("book", PATH)
				.param("lang", "es")
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("id"))
				.andExpect(jsonPath("$.title").value("title"))
				.andExpect(jsonPath("$.path").value("path"))
				.andExpect(jsonPath("$.serie.index").value(1))
				.andExpect(jsonPath("$.serie.name").value("Serie1"))
				.andExpect(jsonPath("$.pubDate").value("01/01/2000"))
				.andExpect(jsonPath("$.pages").value(0))
				.andExpect(jsonPath("$.rating").value(0.0))
				.andExpect(jsonPath("$.authors[0]").value("author"))
				.andExpect(jsonPath("$.tags[0]").value("tag"))
				.andExpect(jsonPath("$.similar[0]").value("similar"))
				.andExpect(jsonPath("$.languages[0]").value("es"));

		Mockito.verify(commandBus, Mockito.times(1)).executeAndWait(any(FindBookMetadataCommand.class));
		Mockito.verify(commandBus, Mockito.times(1)).executeAndWait(any(FindReviewMetadataCommand.class));
	}

	private void insertBook() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("id")
				.title(TITLE)
				.path(PATH)
				.languages(List.of("es"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.serie(SerieMongo.builder().index(1).name("Serie1").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image("::image::")
				.build();
		bookRepository.save(bookMongoEntity);
	}

}
