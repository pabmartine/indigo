package com.martinia.indigo.file.application;

import com.martinia.indigo.file.domain.ports.usecases.CountEpubFilesUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

@Service
@Transactional
@Slf4j
public class CountEpubFilesUseCaseImpl implements CountEpubFilesUseCase {

	private static final Duration CACHE_TTL = Duration.ofSeconds(30);

	private volatile long cachedCount = -1L;
	private volatile Instant lastCountAt = Instant.EPOCH;

	@Value("${book.library.uploads}")
	private String uploadsPath;

	@Override
	public Long count() {
		Instant now = Instant.now();
		if (cachedCount >= 0 && Duration.between(lastCountAt, now).compareTo(CACHE_TTL) < 0) {
			return cachedCount;
		}

		try {
			Path path = Paths.get(uploadsPath);

			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}

			long count;
			try (var files = Files.walk(path)) {
				count = files.filter(file -> file.toFile().getName().toLowerCase().endsWith(".epub")).count();
			}
			cachedCount = count;
			lastCountAt = now;
			return count;

		}
		catch (IOException e) {
			log.error(e.getMessage());
			return 0L;
		}
	}
}

