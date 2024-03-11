package com.martinia.indigo.file.application.events;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.ports.usecases.events.DeleteEpubFileEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Transactional
@Slf4j
public class DeleteEpubFileEventUseCaseImpl implements DeleteEpubFileEventUseCase {

	@Value("${book.library.uploads}")
	private String uploadsPath;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	@Transactional
	public void delete(final Path path) {
		try {
			if (path.toString().contains(uploadsPath) && !path.toString().equals(uploadsPath)) {

				File file = path.toFile();
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (File f : files) {
						if (f.isFile() && !file.toString().endsWith(".epub")) {
							Files.delete(f.toPath());
						}
					}
				}

				if (isEmptyDirectory(path)) {
					Files.delete(path);
				}

				delete(path.getParent());

				uploadEpubFilesSingleton.addDelete();
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
			uploadEpubFilesSingleton.addDeleteError();
		}
	}

	private boolean isEmptyDirectory(Path dir) throws IOException {
		try (var stream = Files.list(dir)) {
			return !stream.anyMatch(path -> !Files.isDirectory(path) && !path.getFileName().toString().endsWith(".epub"));
		}
	}

}




