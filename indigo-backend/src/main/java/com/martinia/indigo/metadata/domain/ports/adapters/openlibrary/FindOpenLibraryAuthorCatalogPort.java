package com.martinia.indigo.metadata.domain.ports.adapters.openlibrary;

public interface FindOpenLibraryAuthorCatalogPort {

	String[] findAuthor(String name);
	boolean isAvailable();

	default String[] findAuthor(String name, boolean descriptionNeeded) {
		return findAuthor(name);
	}
}
