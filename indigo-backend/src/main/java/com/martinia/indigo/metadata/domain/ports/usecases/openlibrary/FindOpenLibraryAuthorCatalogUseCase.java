package com.martinia.indigo.metadata.domain.ports.usecases.openlibrary;

public interface FindOpenLibraryAuthorCatalogUseCase {
	String[] findAuthor(String name);
	boolean isAvailable();

	default String[] findAuthor(String name, boolean descriptionNeeded) {
		return findAuthor(name);
	}
}
