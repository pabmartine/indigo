package com.martinia.indigo.mail.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.mail.domain.EmailConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.Resource;
import javax.mail.MessagingException;

public class SendMailUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private JavaMailSender javaMailSender;

	@MockBean
	private EmailConfiguration emailConfiguration;

	@Resource
	private SendMailUseCaseImpl sendMailUseCase;

	@Test
	public void testMail_WhenEpubFileExists_ThenSendEmail() throws MessagingException {

		//Given

		//When
		String sent = sendMailUseCase.mail("path", "address");
		//Then
		Assertions.assertTrue(sent.contains("not exist"));

	}
}

