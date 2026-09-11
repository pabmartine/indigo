package com.martinia.indigo.metadata.infrastructure.adapters.openlibrary;

import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorPort;
import com.martinia.indigo.metadata.domain.ports.usecases.openlibrary.FindOpenLibraryAuthorUseCase;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "flags.openlibrary", havingValue = "true")
public class FindOpenLibraryAuthorAdapter implements FindOpenLibraryAuthorPort {

	@Resource
	private FindOpenLibraryAuthorUseCase useCase;

	@Override
	public String[] findAuthor(final String name) {
		return useCase.findAuthor(name);
	}
}
