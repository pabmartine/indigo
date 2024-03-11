package com.martinia.indigo.mail.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.mail.domain.ports.usecases.SendMailUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SendMailUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private SendMailUseCase sendMailUseCase;

	@MockBean
	private JavaMailSender javaMailSender;

	@MockBean
	private ConfigurationRepository configurationRepository;

	@Value("${book.library.path}")
	private String libraryPath;

	@Test
	public void testMail_WithInvalidPath_ShouldReturnError() {
		// Given
		String path = "invalid_path";
		String address = "test@example.com";

		// When
		sendMailUseCase.mail(path, address);

		// Then
		verify(javaMailSender, never()).send(any(MimeMessage.class));
	}

}


