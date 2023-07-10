package com.martinia.indigo.mail.domain.ports.usecases;

public interface SendMailUseCase {
	String mail(final String path, final String user);

}
