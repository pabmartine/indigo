package com.martinia.indigo.author.infrastructure.api.controllers.cover;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.usecases.cover.FindAuthorCoverByIdUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import jakarta.annotation.Resource;
import java.util.Optional;

public class FindAuthorCoverByIdControllerTest extends BaseIndigoTest {

	@MockBean
	private FindAuthorCoverByIdUseCase mockUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testGetCover_WhenCoverExists_ThenReturnOkWithCacheControl() throws Exception {
		byte[] coverBytes = new byte[] { 1, 2, 3, 4 };
		Mockito.when(mockUseCase.getCover("author123")).thenReturn(Optional.of(coverBytes));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/author/cover/author123"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.header().string("Cache-Control", "max-age=2592000, public"))
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG_VALUE))
				.andExpect(MockMvcResultMatchers.content().bytes(coverBytes));

		Mockito.verify(mockUseCase).getCover("author123");
	}

	@Test
	@WithMockUser
	public void testGetCover_WhenCoverNotFound_ThenReturnNotFound() throws Exception {
		Mockito.when(mockUseCase.getCover("unknown")).thenReturn(Optional.empty());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/author/cover/unknown"))
				.andExpect(MockMvcResultMatchers.status().isNotFound());

		Mockito.verify(mockUseCase).getCover("unknown");
	}
}
