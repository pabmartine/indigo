package com.martinia.indigo.common.configuration.infrastructure.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationDto implements Serializable {

	private static final long serialVersionUID = 3210483964355113351L;

	private String key;
	private String value;

}
