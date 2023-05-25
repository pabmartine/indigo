package com.martinia.indigo.common.mail.application;

import com.martinia.indigo.domain.beans.EmailConfiguration;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;

import javax.annotation.Resource;

public class BaseMailUseCaseImpl {

	@Resource
	protected ConfigurationRepository configurationRepository;

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
