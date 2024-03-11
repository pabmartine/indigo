package com.martinia.indigo.file.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.commands.ExtractEpubFileCommand;
import com.martinia.indigo.file.domain.ports.usecases.UploadEpubFilesUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional
@Slf4j
public class UploadEpubFilesUseCaseImpl implements UploadEpubFilesUseCase {

	@Value("${book.library.uploads}")
	private String uploadsPath;

	@Resource
	private CommandBus commandBus;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	public void upload(final Long number) {
		try {
			final Path path = Paths.get(uploadsPath);
			uploadEpubFilesSingleton.start(number);

			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}

			Files.walk(path)
					.filter(file -> file.toFile().getName().toLowerCase().endsWith(".epub"))
					.limit(number)
					.map(Path::toAbsolutePath)
					.forEach(file -> {
						commandBus.execute(ExtractEpubFileCommand.builder().file(file).build());
					});

		}
		catch (Exception e) {
			uploadEpubFilesSingleton.stop();
			log.error(e.getMessage());
		}
	}
}


