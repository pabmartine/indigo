package com.martinia.indigo.file.domain.ports.usecases.commands;

import java.nio.file.Path;

public interface ExtractEpubFileUseCase {

	void extract(Path path);

}
