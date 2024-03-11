package com.martinia.indigo.configuration.infrastructure.mongo.entities;

import java.io.Serializable;

import javax.persistence.Id;

import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "configurations")
public class ConfigurationMongoEntity implements Serializable {

	private static final long serialVersionUID = 2964788136769162180L;

	@Id
	private String id;
	private String key;
	private String value;

}
