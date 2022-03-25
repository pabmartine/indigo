package com.martinia.indigo.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.beans.EmailConfiguration;
import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.ports.in.rest.MailService;
import com.martinia.indigo.ports.out.mail.MailSender;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;

@Service
public class MailServiceImpl implements MailService {

	@Autowired
	MailSender mailSender;

	@Autowired
	ConfigurationRepository configurationRepository;

	private EmailConfiguration getEmailConfig() {
		EmailConfiguration config = new EmailConfiguration();
		config.setHost(configurationRepository.findByKey("smtp.host").getValue());
		config.setPort(Integer.parseInt(configurationRepository.findByKey("smtp.port").getValue()));
		config.setUsername(configurationRepository.findByKey("smtp.username").getValue());
		config.setPasswor(configurationRepository.findByKey("smtp.password").getValue());
		config.setEncryption(configurationRepository.findByKey("smtp.encryption").getValue());
		config.setKindlegen(configurationRepository.findByKey("kindlegen.path").getValue());
		return config;
	}

	@Override
	public void testEmail(String address) {
		boolean ret = mailSender.testEmail(address, getEmailConfig());
		Configuration configuration = configurationRepository.findByKey("smtp.status");
		
		if (configuration == null) {
			configuration = new Configuration("smtp.status", ret ? "ok" : "error");
		} else
			configuration.setValue(ret ? "ok" : "error");
		
		configurationRepository.save(configuration);
	}

	@Override
	public String mail(String path, String address) {
		return mailSender.mail(path, address, getEmailConfig());
	}

}
