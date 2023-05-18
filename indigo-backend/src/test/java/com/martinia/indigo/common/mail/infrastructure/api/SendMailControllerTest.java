package com.martinia.indigo.common.mail.infrastructure.api;

import com.martinia.indigo.common.mail.domain.service.SendMailUseCase;
import org.junit.jupiter.api.Test;
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
class SendMailControllerTest {

	@Resource
	private MockMvc mockMvc;

	@MockBean
	private SendMailUseCase sendMailUseCase;

	@Test
	void testSendMail_Success() throws Exception {
		String path = "testPath";
		String address = "test@example.com";

		mockMvc.perform(get("/rest/mail/send").param("path", path).param("address", address)).andExpect(status().isOk());

		verify(sendMailUseCase).mail(path, address);
	}

}




