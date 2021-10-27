package com.martinia.indigo.domain.model;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Configuration implements Serializable {


	private static final long serialVersionUID = 2964788136769162180L;

	private String id;
	private String key;
	private String value;
	
	public Configuration(String key, String value) {
		this.id = UUID.randomUUID().toString();
		this.key = key;
		this.value = value;
	}
}