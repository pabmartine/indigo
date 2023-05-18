package com.martinia.indigo.common.mail.infrastructure.api;

import com.martinia.indigo.common.mail.domain.service.SendTestMailUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SendTestMailControllerTest {

	@Resource
	private MockMvc mockMvc;

	@MockBean
	private SendTestMailUseCase sendTestMailUseCase;

	@Test
	void testSendTestMail() throws Exception {
		String address = "test@example.com";

		mockMvc.perform(get("/rest/mail/test").param("address", address)).andExpect(status().isOk());

		verify(sendTestMailUseCase).test(address);
	}

}

