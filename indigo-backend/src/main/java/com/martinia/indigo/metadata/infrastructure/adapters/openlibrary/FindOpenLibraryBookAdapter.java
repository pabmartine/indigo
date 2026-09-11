package com.martinia.indigo.metadata.infrastructure.adapters.openlibrary;

import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryBookPort;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;
import com.martinia.indigo.metadata.domain.ports.usecases.openlibrary.FindOpenLibraryBookUseCase;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "flags.openlibrary", havingValue = "true")
public class FindOpenLibraryBookAdapter implements FindOpenLibraryBookPort {

	@Resource
	private FindOpenLibraryBookUseCase useCase;

	@Override
	public BookMetadataResult findBook(final String title, final List<String> authors) {
		return useCase.findBook(title, authors);
	}

	@Override
	public BookMetadataResult findBook(final BookMetadataQuery query) {
		return useCase.findBook(query);
	}
}
