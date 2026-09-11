package com.martinia.indigo.metadata.domain.ports.usecases.openlibrary;

import java.util.List;

import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;

public interface FindOpenLibraryBookUseCase {

	BookMetadataResult findBook(String title, List<String> authors);

	default BookMetadataResult findBook(final BookMetadataQuery query) {
		return findBook(query.getTitle(), query.getAuthors());
	}
}
