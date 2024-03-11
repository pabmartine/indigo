package com.martinia.indigo.file.domain.ports.usecases.events;

import java.nio.file.Path;

public interface MoveEpubFileEventUseCase {

	void move(final Path sourcePath, final Path targetPath);

}
