package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindCoverSerieControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void findCoverSerieNotFound() throws Exception {
		// Given
		String serie = "TestSerie";

		// When
		mockMvc.perform(get("/api/serie/cover").param("serie", serie))
				.andExpect(status().isNotFound());

	}

	@Test
	public void findCoverSerieOk() throws Exception {
		// Given
		String serie = "TestSerie";

		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("64dce11b1520b348ff4b96ae")
				.title("title")
				.path("path")
				.languages(List.of("spa"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.serie(SerieMongo.builder().index(1).name("TestSerie").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image(Base64.getEncoder().encodeToString("cover-data".getBytes(StandardCharsets.UTF_8)))
				.build();
		bookRepository.save(bookMongoEntity);

		// When
		byte[] expected = "cover-data".getBytes(StandardCharsets.UTF_8);
		mockMvc.perform(get("/api/serie/cover").param("serie", serie))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG))
				.andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(expected));

	}
}
