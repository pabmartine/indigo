package com.martinia.indigo.metadata.infrastructure.adapters.openlibrary;

import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorCatalogPort;
import com.martinia.indigo.metadata.domain.ports.usecases.openlibrary.FindOpenLibraryAuthorCatalogUseCase;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "flags.openlibrary", havingValue = "true")
public class FindOpenLibraryAuthorCatalogAdapter implements FindOpenLibraryAuthorCatalogPort {
	@Resource
	private FindOpenLibraryAuthorCatalogUseCase useCase;

	@Override
	public boolean isAvailable() { return useCase.isAvailable(); }

	@Override
	public String[] findAuthor(String name, boolean descriptionNeeded) {
		return useCase.findAuthor(name, descriptionNeeded);
	}

	@Override
	public String[] findAuthor(final String name) {
		return useCase.findAuthor(name);
	}
}
