package com.martinia.indigo.domain.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailConfiguration {

	private String host;
	private int port;
	private String username;
	private String passwor;
	private String encryption;
	private String kindlegen;
}
