package com.martinia.indigo.metadata.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "metadata.autostart")
public class MetadataAutostartProperties {

	private boolean enabled;
	private Type type = Type.AUTHORS_EMPTY;
	private String lang = "es";

	@Getter
	public enum Type {
		AUTHORS_ALL("FULL", "AUTHORS"),
		AUTHORS_EMPTY("PARTIAL", "AUTHORS"),
		BOOKS_ALL("FULL", "BOOKS"),
		BOOKS_EMPTY("PARTIAL", "BOOKS"),
		REVIEWS_ALL("FULL", "REVIEWS"),
		REVIEWS_EMPTY("PARTIAL", "REVIEWS");

		private final String processType;
		private final String entity;

		Type(String processType, String entity) {
			this.processType = processType;
			this.entity = entity;
		}
	}
}
