package com.martinia.indigo.file.domain.ports.usecases.events;

public interface SaveTagEpubFileEventUseCase {

	void save(final String bookId);

	void save(final String bookId, final boolean newBook);

}
