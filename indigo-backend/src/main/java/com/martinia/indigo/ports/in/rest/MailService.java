package com.martinia.indigo.ports.in.rest;

public interface MailService {

	void testEmail(String user);

	String mail(String path, String user);

}
