package com.martinia.indigo.user.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.UpdateUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class UpdateUserControllerTest extends BaseIndigoTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UpdateUserUseCase useCase;

	@Test
	@WithMockUser
	public void testUpdateUser_Success() throws Exception {
		User user = new User();
		String userJson = objectMapper.writeValueAsString(user);

		mockMvc.perform(MockMvcRequestBuilders.put("/rest/user/update").contentType(MediaType.APPLICATION_JSON).content(userJson))
				.andExpect(MockMvcResultMatchers.status().isOk());

		verify(useCase).update(any());
	}

}