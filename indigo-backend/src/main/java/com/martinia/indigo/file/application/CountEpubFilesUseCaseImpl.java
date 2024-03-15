package com.martinia.indigo.file.application;

import com.martinia.indigo.file.domain.ports.usecases.CountEpubFilesUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional
@Slf4j
public class CountEpubFilesUseCaseImpl implements CountEpubFilesUseCase {

	@Value("${book.library.uploads}")
	private String uploadsPath;

	@Override
	public Long count() {
		try {
			Path path = Paths.get(uploadsPath);

			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}

			return Files.walk(path).filter(file -> file.toFile().getName().toLowerCase().endsWith(".epub")).count();

		}
		catch (IOException e) {
			log.error(e.getMessage());
			return 0L;
		}
	}
}


