package com.martinia.indigo.file.domain.ports.usecases.events;

import java.nio.file.Path;

public interface DeleteEpubFileEventUseCase {
	void delete(final Path sourcePath);

}
