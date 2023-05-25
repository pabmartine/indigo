package com.martinia.indigo.common.mail.application;

import com.martinia.indigo.common.mail.domain.service.SendTestMailUseCase;
import com.martinia.indigo.common.configuration.domain.model.Configuration;
import com.martinia.indigo.ports.out.mail.MailSenderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class SendTestMailUseCaseImpl extends BaseMailUseCaseImpl implements SendTestMailUseCase {

	@Resource
	private MailSenderService mailSenderService;

	@Override
	public void test(final String address) {
		boolean ret = mailSenderService.testEmail(address, getEmailConfig());
		Optional<Configuration> configuration = configurationRepository.findByKey("smtp.status");

		configurationRepository.save(configuration.map(conf -> {
			conf.setValue(ret ? "ok" : "error");
			return conf;
		}).orElse(new Configuration("smtp.status", ret ? "ok" : "error")));
	}

}
