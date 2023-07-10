package com.martinia.indigo.mail.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.mail.domain.ports.usecases.SendTestMailUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SendTestMailControllerTest extends BaseIndigoTest {

	@MockBean
	private SendTestMailUseCase sendTestMailUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	public void testTestMail_Successful() throws Exception {
		// Given
		String address = "example_address";

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/mail/test").param("address", address))
				.andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		// Verify the method invocation
		verify(sendTestMailUseCase, times(1)).test(address);
	}
}
