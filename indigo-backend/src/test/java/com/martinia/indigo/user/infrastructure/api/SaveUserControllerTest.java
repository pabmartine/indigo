package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.SaveUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;

public class SaveUserControllerTest  extends BaseIndigoTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SaveUserUseCase saveUserUseCase;

	@Test
	@WithMockUser
	public void testSaveUser() throws Exception {
		User user = new User();
		user.setUsername("testuser");
		user.setPassword("password");

		mockMvc.perform(MockMvcRequestBuilders.post("/rest/user/save").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"testuser\",\"password\":\"password\"}")).andExpect(MockMvcResultMatchers.status().isOk());

		verify(saveUserUseCase).save(any(), anyBoolean());
	}
}