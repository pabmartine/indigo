package com.martinia.indigo.tag.infrastructure.api.controllers.cover;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.model.TagCoverResult;
import com.martinia.indigo.tag.domain.ports.usecases.cover.FindTagCoverByIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.annotation.Resource;
import java.net.URI;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FindTagCoverByIdControllerTest extends BaseIndigoTest {

	@Resource
	private MockMvc mockMvc;

	@MockBean
	private FindTagCoverByIdUseCase useCase;

	@Resource
	private FindTagCoverByIdController controller;

	@BeforeEach
	public void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void shouldReturnCoverWhenFound() throws Exception {
		byte[] coverBytes = new byte[]{1, 2, 3};
		when(useCase.getCover("tag-1")).thenReturn(TagCoverResult.bytes(coverBytes));

		mockMvc.perform(get("/api/tag/cover/tag-1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG))
				.andExpect(header().string("Cache-Control", "max-age=2592000, public"))
				.andExpect(content().bytes(coverBytes));
	}

	@Test
	public void shouldReturnRedirectWhenUrl() throws Exception {
		URI redirectUri = URI.create("https://example.com/image.jpg");
		when(useCase.getCover("tag-2")).thenReturn(TagCoverResult.redirect(redirectUri));

		mockMvc.perform(get("/api/tag/cover/tag-2"))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://example.com/image.jpg"));
	}

	@Test
	public void shouldReturnNotFoundWhenMissing() throws Exception {
		when(useCase.getCover("tag-empty")).thenReturn(TagCoverResult.empty());

		mockMvc.perform(get("/api/tag/cover/tag-empty"))
				.andExpect(status().isNotFound());
	}
}
