package com.martinia.indigo.file.domain.ports.usecases.events;

import com.martinia.indigo.common.domain.model.BookOpf;

import java.nio.file.Path;

public interface SaveBookEpubFileExtractedEventUseCase {
	void save(final BookOpf bookOpf, final Path path);

}
