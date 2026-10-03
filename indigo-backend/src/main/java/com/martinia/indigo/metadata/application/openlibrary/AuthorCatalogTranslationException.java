package com.martinia.indigo.metadata.application.openlibrary;

public class AuthorCatalogTranslationException extends com.martinia.indigo.metadata.application.AuthorMetadataTranslationException {
	public AuthorCatalogTranslationException(String message) {
		this(message, null, null);
	}

	public AuthorCatalogTranslationException(String message, String image, Throwable cause) {
		super(message, image, "OPEN_LIBRARY", cause);
	}
}
