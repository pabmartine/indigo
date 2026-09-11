package com.martinia.indigo.metadata.domain.ports.adapters.openlibrary;

import java.util.List;

import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;

public interface FindOpenLibraryBookPort {

	BookMetadataResult findBook(String title, List<String> authors);

	default BookMetadataResult findBook(final BookMetadataQuery query) {
		return findBook(query.getTitle(), query.getAuthors());
	}
}
