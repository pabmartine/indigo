package com.martinia.indigo.model.indigo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "\"configurations\"")
public class Configuration {

	@Id
	@Column(name = "\"key\"")
	private String key;

	@Column(name = "\"value\"")
	private String value;

	public Configuration() {
		super();
	}

	public Configuration(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
