package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.ports.usecases.DeleteUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.verify;


public class DeleteUserControllerTest extends BaseIndigoTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DeleteUserUseCase deleteUserUseCase;

	@Test
	@WithMockUser
	public void testDeleteUser() throws Exception {
		String userId = "12345";

		mockMvc.perform(MockMvcRequestBuilders.delete("/rest/user/delete").param("id", userId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());

		verify(deleteUserUseCase).delete(userId);

	}
}
