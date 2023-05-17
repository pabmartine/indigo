package com.martinia.indigo.user.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.user.domain.service.UpdateUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureMockMvc
public class UpdateUserControllerTest {

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

	//	@Test
	//	public void testUpdateUser_ReturnsBadRequest() throws Exception {
	//		User user = new User();
	//		String userJson = objectMapper.writeValueAsString(user);
	//
	//		MvcResult result = mockMvc
	//				.perform(MockMvcRequestBuilders.put("/rest/user/update").contentType(MediaType.APPLICATION_JSON).content(userJson))
	//				.andReturn();
	//
	//		assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
	//		assertEquals("Invalid request", result.getResponse().getContentAsString());
	//	}

}