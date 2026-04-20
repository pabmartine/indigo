package com.martinia.indigo.file.domain.ports.usecases.events;

public interface SaveAuthorEpubFileEventUseCase {

	void save(final String bookId, final String authorImage);

	void save(final String bookId, final String authorImage, final boolean newBook);

}
