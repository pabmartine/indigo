package com.martinia.indigo.common.mail.application;

import com.martinia.indigo.common.mail.domain.service.SendMailUseCase;
import com.martinia.indigo.ports.out.mail.MailSenderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SendMailUseCaseImpl extends BaseMailUseCaseImpl implements SendMailUseCase {

	@Resource
	private MailSenderService mailSenderService;

	@Override
	public String mail(final String path, final String address) {
		return mailSenderService.mail(path.replace("@_@", "&").replace("@-@", "[").replace("@¡@", "]").replace("@!@", "`"), address,
				getEmailConfig());
	}

}
