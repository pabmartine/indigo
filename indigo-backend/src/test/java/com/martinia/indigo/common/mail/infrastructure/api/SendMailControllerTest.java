package com.martinia.indigo.common.mail.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.mail.domain.ports.usecases.SendMailUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SendMailControllerTest extends BaseIndigoTest {

	@MockBean
	private SendMailUseCase useCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	public void testSend_When_MailSentSuccessfully_Then_ReturnOkStatus() throws Exception {
		// Given
		String path = "samplePath";
		String address = "sampleAddress";

		when(useCase.mail(eq(path), eq(address))).thenReturn(null);

		// When-Then
		mockMvc.perform(get("/api/mail/send").param("path", path).param("address", address).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(useCase, times(1)).mail(eq(path), eq(address));
	}

	@Test
	public void testSend_When_MailSendingFailed_Then_ThrowException() throws Exception {
		// Given
		String path = "samplePath";
		String address = "sampleAddress";
		String error = "Mail sending failed";

		when(useCase.mail(eq(path), eq(address))).thenReturn(error);

		// When-Then
		mockMvc.perform(get("/api/mail/send").param("path", path).param("address", address).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof Exception))
				.andExpect(result -> assertEquals(error, result.getResolvedException().getMessage()));

		verify(useCase, times(1)).mail(eq(path), eq(address));
	}
}



