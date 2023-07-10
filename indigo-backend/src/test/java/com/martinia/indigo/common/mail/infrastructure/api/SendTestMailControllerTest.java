package com.martinia.indigo.common.mail.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.mail.domain.ports.usecases.SendTestMailUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class SendTestMailControllerTest extends BaseIndigoTest {

	@Resource
	private MockMvc mockMvc;

	@MockBean
	private SendTestMailUseCase sendTestMailUseCase;

	@Test
	void testSendTestMail() throws Exception {
		String address = "test@example.com";

		mockMvc.perform(get("/api/mail/test").param("address", address)).andExpect(status().isOk());

		verify(sendTestMailUseCase).test(address);
	}

}

