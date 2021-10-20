package com.martinia.indigo.ports.in.rest;

public interface MailService {

	public void testEmail(String user);

	public String mail(String path, String user);

}
