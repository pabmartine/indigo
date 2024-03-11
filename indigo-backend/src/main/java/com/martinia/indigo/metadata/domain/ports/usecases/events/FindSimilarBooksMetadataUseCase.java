package com.martinia.indigo.metadata.domain.ports.usecases.events;

public interface FindSimilarBooksMetadataUseCase {

	void findSimilarBooks(final String bookId, final String similar);
}
