package com.martinia.indigo.metadata.domain.ports.usecases.events;

public interface BookLoadedEventUseCase {
	void fillAuthors(String bookId);
}
