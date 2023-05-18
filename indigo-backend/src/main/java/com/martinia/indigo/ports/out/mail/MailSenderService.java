package com.martinia.indigo.ports.out.mail;

import com.martinia.indigo.domain.beans.EmailConfiguration;

public interface MailSenderService {

	boolean testEmail(String address, EmailConfiguration emailConfig);

	String mail(String path, String address, EmailConfiguration emailConfig);

}
