package com.martinia.indigo.mail.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.mail.domain.ports.usecases.SendMailUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SendMailControllerTest extends BaseIndigoTest {

	@MockBean
	private SendMailUseCase sendMailUseCase;
	@Resource
	private MockMvc mockMvc;

	@Test
	public void testSend_Successful() throws Exception {
		// Given
		String path = "example_path";
		String address = "example_address";
		String error = null;

		when(sendMailUseCase.mail(path, address)).thenReturn(error);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/mail/send").param("path", path).param("address", address))
				.andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		// Verify the method invocation
		verify(sendMailUseCase, times(1)).mail(path, address);
	}

	@Test
	public void testSend_Failure() throws Exception {
		// Given
		String path = "example_path";
		String address = "example_address";
		String error = "example_error";

		when(sendMailUseCase.mail(path, address)).thenReturn(error);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/mail/send").param("path", path).param("address", address))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());

		// Then
		// Verify the method invocation
		verify(sendMailUseCase, times(1)).mail(path, address);
	}
}
