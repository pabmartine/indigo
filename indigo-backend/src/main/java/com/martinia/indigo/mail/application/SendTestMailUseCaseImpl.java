package com.martinia.indigo.mail.application;

import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.mail.domain.EmailConfiguration;
import com.martinia.indigo.mail.domain.ports.usecases.SendTestMailUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class SendTestMailUseCaseImpl extends BaseMailUseCaseImpl implements SendTestMailUseCase {

	@Override
	public void test(final String address) {
		boolean ret = send(address, getEmailConfig());
		Optional<ConfigurationMongoEntity> configuration = configurationMongoRepository.findByKey("smtp.status");

		configurationMongoRepository.save(configuration.map(conf -> {
			conf.setValue(ret ? "ok" : "error");
			return conf;
		}).orElse(ConfigurationMongoEntity.builder().key("smtp.status").value(ret ? "ok" : "error").build()));
	}

	public boolean send(String address, EmailConfiguration emailConfig) {
		boolean ret = false;
		try {

			init(emailConfig);

			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(address);
			message.setFrom("no-Reply@indigo.com");
			message.setSubject("Test mail");
			message.setText("This is a test mail");

			javaMailSender.send(message);

			ret = true;
		}
		catch (MailException e) {
			log.error(e.getMessage());
		}
		return ret;
	}

}
