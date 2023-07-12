package com.martinia.indigo.mail.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.Optional;

class SendTestMailUseCaseImplTest extends BaseIndigoTest {

	@SpyBean
	private JavaMailSender javaMailSender;

	@MockBean
	private ConfigurationRepository configurationRepository;

	@Resource
	private SendTestMailUseCaseImpl sendMailUseCase;

	@Test
	public void testMail_WhenEpubFileExists_ThenSendEmail() throws MessagingException {

		//Given
		Mockito.doNothing().when(javaMailSender).send(Mockito.any(SimpleMailMessage.class));
		Mockito.when(configurationRepository.findByKey(Mockito.any())).thenReturn(Optional.empty());
		final ConfigurationMongoEntity confEncryption = ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build();
		final Optional<ConfigurationMongoEntity> optConfEncryption = Optional.of(confEncryption);
		Mockito.when(configurationRepository.findByKey("smtp.encryption")).thenReturn(optConfEncryption);
		//When
		sendMailUseCase.test("address");
		//Then
		Mockito.verify(configurationRepository, Mockito.atLeast(1)).findByKey("smtp.status");
		Mockito.verify(configurationRepository, Mockito.atLeast(2)).save(Mockito.any());

	}
}