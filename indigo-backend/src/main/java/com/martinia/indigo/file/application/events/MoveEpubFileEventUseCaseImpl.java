package com.martinia.indigo.file.application.events;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.FileRepository;
import com.martinia.indigo.file.domain.events.EpubFileDeletedEvent;
import com.martinia.indigo.file.domain.model.File;
import com.martinia.indigo.file.domain.ports.usecases.events.MoveEpubFileEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class MoveEpubFileEventUseCaseImpl implements MoveEpubFileEventUseCase {

	public static final String IMAGE = "cover.jpg";
	@Value("${book.library.uploads}")
	private String uploadsPath;
	@Resource
	private ApplicationEventPublisher eventPublisher;
	@Resource
	private FileRepository fileRepository;
	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	@Transactional
	public void move(final Path sourcePath, final Path targetPath) {
		try {

			final Path targetFilePath = Path.of(targetPath + FileSystems.getDefault().getSeparator() + sourcePath.getFileName());
			final Path sourceCoverPath = Path.of(sourcePath.getParent() + FileSystems.getDefault().getSeparator() + IMAGE);
			final Path targetCoverPath = Path.of(targetPath + FileSystems.getDefault().getSeparator() + IMAGE);

			final Optional<File> file = fileRepository.findByPath(sourcePath);

			if (Files.exists(sourcePath) && Files.exists(sourceCoverPath)) {

				if (!Files.exists(targetPath)) {
					Files.createDirectories(targetPath);
				}

				Files.move(sourcePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
				Files.move(sourceCoverPath, targetCoverPath, StandardCopyOption.REPLACE_EXISTING);

				if (file.isPresent()) {
					eventPublisher.publishEvent(new EpubFileDeletedEvent(file.get().getId()));
				}

				uploadEpubFilesSingleton.addMove();
			}
			else {
				log.error("File {} or Image {} does not exist", sourcePath, sourceCoverPath);
				uploadEpubFilesSingleton.addMoveError();
			}

		}
		catch (Exception e) {
			uploadEpubFilesSingleton.addMoveError();
			log.error(e.getMessage());
		}
	}

}

