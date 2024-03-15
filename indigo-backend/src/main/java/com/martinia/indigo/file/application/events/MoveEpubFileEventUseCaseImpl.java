package com.martinia.indigo.file.application.events;

import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.events.FileMovedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.MoveEpubFileEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Transactional
@Slf4j
public class MoveEpubFileEventUseCaseImpl implements MoveEpubFileEventUseCase {

	public static final String IMAGE = "cover.jpg";
	@Value("${book.library.uploads}")
	private String uploadsPath;
	@Resource
	private EventBus eventBus;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	@Transactional
	public void move(final Path sourcePath, final Path targetPath) {
		try {

			final Path targetFilePath = Path.of(targetPath + FileSystems.getDefault().getSeparator() + sourcePath.getFileName());
			final Path sourceCoverPath = Path.of(sourcePath.getParent() + FileSystems.getDefault().getSeparator() + IMAGE);
			final Path targetCoverPath = Path.of(targetPath + FileSystems.getDefault().getSeparator() + IMAGE);

			if (Files.exists(sourcePath) && Files.exists(sourceCoverPath)) {

				if (!Files.exists(targetPath)) {
					Files.createDirectories(targetPath);
				}

				Files.move(sourcePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
				Files.move(sourceCoverPath, targetCoverPath, StandardCopyOption.REPLACE_EXISTING);

				uploadEpubFilesSingleton.addMove();
				eventBus.publish(FileMovedEvent.builder().sourcePath(sourcePath.getParent()).build());
			}
			else {
				log.error("File {} or Image {} does not exist", sourcePath, sourceCoverPath);
				uploadEpubFilesSingleton.addMoveError();
			}

		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}


