package com.martinia.indigo.common.mail.application;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.martinia.indigo.domain.beans.EmailConfiguration;
import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.ports.out.mail.MailSenderService;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { SendTestMailUseCaseImpl.class })
@ExtendWith(SpringExtension.class)
class SendTestMailUseCaseImplTest {
	@MockBean
	private ConfigurationRepository configurationRepository;

	@MockBean
	private MailSenderService mailSenderService;

	@Autowired
	private SendTestMailUseCaseImpl sendTestMailUseCaseImpl;

	@Test
	void testTest() {
		//Given
		when(configurationRepository.findByKey(Mockito.<String>any())).thenReturn(Optional.of(new Configuration("Key", "42")));
		doNothing().when(configurationRepository).save(Mockito.<Configuration>any());
		when(mailSenderService.testEmail(Mockito.<String>any(), Mockito.<EmailConfiguration>any())).thenReturn(true);

		//When
		sendTestMailUseCaseImpl.test("42 Main St");

		//Then
		verify(configurationRepository, atLeast(1)).findByKey(Mockito.<String>any());
		verify(configurationRepository).save(Mockito.<Configuration>any());
		verify(mailSenderService).testEmail(Mockito.<String>any(), Mockito.<EmailConfiguration>any());
	}

	@Test
	void testTest2() {
		//Given
		when(configurationRepository.findByKey(Mockito.<String>any())).thenReturn(Optional.empty());
		doNothing().when(configurationRepository).save(Mockito.<Configuration>any());
		when(mailSenderService.testEmail(Mockito.<String>any(), Mockito.<EmailConfiguration>any())).thenReturn(true);

		//When
		sendTestMailUseCaseImpl.test("42 Main St");

		//Then
		verify(configurationRepository, atLeast(1)).findByKey(Mockito.<String>any());
		verify(configurationRepository).save(Mockito.<Configuration>any());
		verify(mailSenderService).testEmail(Mockito.<String>any(), Mockito.<EmailConfiguration>any());
	}

	@Test
	void testTest3() {
		//Given
		when(configurationRepository.findByKey(Mockito.<String>any())).thenReturn(Optional.of(new Configuration("Key", "42")));
		doNothing().when(configurationRepository).save(Mockito.<Configuration>any());
		when(mailSenderService.testEmail(Mockito.<String>any(), Mockito.<EmailConfiguration>any())).thenReturn(false);

		//When
		sendTestMailUseCaseImpl.test("42 Main St");

		//Then
		verify(configurationRepository, atLeast(1)).findByKey(Mockito.<String>any());
		verify(configurationRepository).save(Mockito.<Configuration>any());
		verify(mailSenderService).testEmail(Mockito.<String>any(), Mockito.<EmailConfiguration>any());
	}
}

