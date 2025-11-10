package com.martinia.indigo.book.infrastructure.api.cover;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindBookCoverByIdControllerTest extends BaseIndigoTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturnCoverImage() throws Exception {
		byte[] bytes = "controller-cover".getBytes(StandardCharsets.UTF_8);
		BookMongoEntity entity = BookMongoEntity.builder()
				.title("Controller Test")
				.path("/tmp/controller.epub")
				.image(Base64.getEncoder().encodeToString(bytes))
				.build();
		entity = bookRepository.save(entity);

		byte[] response = mockMvc.perform(get("/api/book/cover/{id}", entity.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG))
				.andReturn()
				.getResponse()
				.getContentAsByteArray();

		assertThat(response).isEqualTo(bytes);
	}

	@Test
	void shouldReturnNotFoundWhenCoverMissing() throws Exception {
		mockMvc.perform(get("/api/book/cover/{id}", "507f1f77bcf86cd799439011"))
				.andExpect(status().isNotFound());
	}
}
