package com.martinia.indigo.common.mail.application;

import com.martinia.indigo.common.configuration.domain.model.Configuration;
import com.martinia.indigo.ports.out.mail.MailSenderService;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class SendMailUseCaseImplTest {

	@Mock
	private MailSenderService mailSenderService;

	@Mock
	private ConfigurationRepository configurationRepository;

	@InjectMocks
	private SendMailUseCaseImpl sendMailUseCase;

	public SendMailUseCaseImplTest() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void sendTest() {
		//Given
		String path = "Hello, @_@world@-_@!@";
		String address = "example@example.com";
		String expected = "mail sent successfully";

		when(mailSenderService.mail(anyString(), anyString(), Mockito.any())).thenReturn(expected);
		final Configuration configuration = Configuration.builder().build();
		when(configurationRepository.findByKey(Mockito.anyString())).thenReturn(Optional.of(configuration));

		String result = sendMailUseCase.mail(path, address);

		assertEquals(expected, result);
	}
}

