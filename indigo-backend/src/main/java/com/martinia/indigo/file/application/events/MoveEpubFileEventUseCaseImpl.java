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
import java.nio.file.DirectoryNotEmptyException;
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
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.file.application.PendingImportService pendingImports;

	@Override
	@Transactional
	public void move(final Path sourcePath, final Path targetPath) {
		try {

			final Path targetFilePath = Path.of(targetPath + FileSystems.getDefault().getSeparator() + sourcePath.getFileName());
			final Path sourceCoverPath = Path.of(sourcePath.getParent() + FileSystems.getDefault().getSeparator() + IMAGE);
			final Path targetCoverPath = Path.of(targetPath + FileSystems.getDefault().getSeparator() + IMAGE);

			final Optional<File> file = fileRepository.findByPath(sourcePath);

			if (Files.exists(sourcePath)) {

				if (!Files.exists(targetPath)) {
					Files.createDirectories(targetPath);
				}

				if (Files.exists(targetFilePath)) {
					if (Files.isSameFile(sourcePath, targetFilePath) || Files.mismatch(sourcePath, targetFilePath) != -1) {
						throw new java.io.IOException("Import conflict; both EPUB files preserved: " + sourcePath);
					}
					Files.delete(sourcePath);
					if (Files.exists(sourceCoverPath) && !Files.exists(targetCoverPath)) {
						Files.move(sourceCoverPath, targetCoverPath);
					}
					else {
						Files.deleteIfExists(sourceCoverPath);
					}
					log.info("Book {} already exists at {}; removed duplicate upload", sourcePath, targetFilePath);
				}
				else {
//					Files.move(sourcePath, targetFilePath);
					Files.copy(sourcePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
					if (Files.exists(sourceCoverPath)) {
						Files.move(sourceCoverPath, targetCoverPath, StandardCopyOption.REPLACE_EXISTING);
					}
					else {
						log.warn("Book {} has no cover image; importing EPUB without a local cover", sourcePath);
					}
				}

				if (file.isPresent()) {
					eventPublisher.publishEvent(new EpubFileDeletedEvent(file.get().getId()));
				}

				deleteEmptyUploadDirectories(sourcePath.getParent());
				if (pendingImports != null) pendingImports.fileComplete(sourcePath);
				uploadEpubFilesSingleton.addMove();
			}
			else {
				if (pendingImports != null && Files.isRegularFile(targetFilePath)) {
					if (file.isPresent()) eventPublisher.publishEvent(new EpubFileDeletedEvent(file.get().getId()));
					pendingImports.fileComplete(sourcePath);
					return;
				}
				log.error("EPUB file {} does not exist", sourcePath);
				uploadEpubFilesSingleton.addMoveError();
			}

		}
		catch (Exception e) {
			uploadEpubFilesSingleton.addMoveError();
			log.error(e.getMessage());
		}
	}

	private void deleteEmptyUploadDirectories(final Path sourceDirectory) {
		if (sourceDirectory == null || uploadsPath == null || uploadsPath.isBlank()) {
			return;
		}

		final Path uploadsRoot = Path.of(uploadsPath).toAbsolutePath().normalize();
		Path directory = sourceDirectory.toAbsolutePath().normalize();
		while (!directory.equals(uploadsRoot) && directory.startsWith(uploadsRoot)) {
			try {
				Files.delete(directory);
				directory = directory.getParent();
			}
			catch (DirectoryNotEmptyException e) {
				break;
			}
			catch (Exception e) {
				log.warn("Could not remove empty upload directory {}", directory, e);
				break;
			}
		}
	}

}
