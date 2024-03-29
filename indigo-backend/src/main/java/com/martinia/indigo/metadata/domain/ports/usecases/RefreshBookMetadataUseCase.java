package com.martinia.indigo.metadata.domain.ports.usecases;

import com.martinia.indigo.book.domain.model.Book;

import java.util.Optional;

public interface RefreshBookMetadataUseCase {

	Optional<Book> findBookMetadata(String book, String lang);
}