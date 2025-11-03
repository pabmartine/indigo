package com.martinia.indigo.configuration.infrastructure.api.model;

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

	private String key;
	private String value;

}
