package com.martinia.indigo.mail.application;

import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.mail.domain.EmailConfiguration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.annotation.Resource;
import java.util.Properties;

@Slf4j
public class BaseMailUseCaseImpl {

	@Resource
	protected ConfigurationRepository configurationRepository;

	@Resource
	protected JavaMailSender javaMailSender;

	@Value("${book.library.path}")
	@Setter
	protected String libraryPath;

	protected void init(EmailConfiguration emailConfig) {

		JavaMailSenderImpl ms = (JavaMailSenderImpl) javaMailSender;
		ms.setHost(emailConfig.getHost());
		ms.setPort(emailConfig.getPort());
		ms.setUsername(emailConfig.getUsername());
		ms.setPassword(emailConfig.getPassword());

		Properties props = ms.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");

		String encryption = emailConfig.getEncryption();

		if (encryption==null)
			log.error("encryption not configured");
		if (encryption.equals("starttls")) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
		}
		else {
			if (encryption.equals("ssl/tls")) {
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.socketFactory.port", "465");
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			}
		}

	}

	protected EmailConfiguration getEmailConfig() {
		final EmailConfiguration config = new EmailConfiguration();
		config.setHost(configurationRepository.findByKey("smtp.host").map(host -> host.getValue()).orElse(null));
		config.setPort(
				Integer.parseInt(configurationRepository.findByKey("smtp.port").map(port -> port.getValue()).orElse(String.valueOf(0))));
		config.setUsername(configurationRepository.findByKey("smtp.username").map(username -> username.getValue()).orElse(null));
		config.setPassword(configurationRepository.findByKey("smtp.password").map(password -> password.getValue()).orElse(null));
		config.setEncryption(configurationRepository.findByKey("smtp.encryption").map(encryption -> encryption.getValue()).orElse(null));
		return config;
	}

}
