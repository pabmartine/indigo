package com.martinia.indigo.file.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.commands.ExtractEpubFileCommand;
import com.martinia.indigo.file.domain.ports.usecases.UploadEpubFilesUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private ParallelEpubImporter parallelImporter;

	@Override
	public synchronized void upload(final Long number) {
		if (number == null || number <= 0) {
			log.warn("The number of EPUB files to upload must be greater than zero");
			return;
		}

		if (uploadEpubFilesSingleton.isRunning() || uploadEpubFilesSingleton.isManagedProcessing()) {
			log.warn("Upload process already running");
			return;
		}

		try {
			final Path path = Paths.get(uploadsPath);

			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}

			try (var files = Files.walk(path)) {
				List<Path> epubFiles = files.filter(Files::isRegularFile)
						.filter(file -> file.getFileName().toString().toLowerCase().endsWith(".epub"))
						.limit(number)
						.map(Path::toAbsolutePath)
						.toList();
				uploadEpubFilesSingleton.start(epubFiles.size());
				if (parallelImporter != null) uploadEpubFilesSingleton.beginManagedProcessing();
			CompletableFuture.runAsync(() -> processFiles(epubFiles));
			}
		}
		catch (Exception e) {
			log.error("Could not process EPUB uploads", e);
		}
	}

	private void processFiles(List<Path> epubFiles) {
		try {
			if (parallelImporter != null) { parallelImporter.process(epubFiles); return; }
			for (int index = 0; index < epubFiles.size(); index++) {
				Path file = epubFiles.get(index);
				try {
					commandBus.executeAndWait(ExtractEpubFileCommand.builder().file(file).build());
				}
				catch (RuntimeException exception) {
					uploadEpubFilesSingleton.addExtractError();
					log.error("Could not import EPUB file {}", file, exception);
				}
				awaitFileCompletion(index + 1, file);
			}
		}
		catch (RuntimeException failure) {
			log.error("Import batch did not finish all related tasks; pending imports can be retried", failure);
		}
		finally {
			if (parallelImporter != null) uploadEpubFilesSingleton.endManagedProcessing();
			else uploadEpubFilesSingleton.stop();
		}
	}

	private void awaitFileCompletion(long expectedProcessed, Path file) {
		long deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(2);
		while (uploadEpubFilesSingleton.isRunning()
				&& uploadEpubFilesSingleton.getProcessedItems() < expectedProcessed
				&& System.nanoTime() < deadline) {
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException interruptedException) {
				Thread.currentThread().interrupt();
				log.warn("Interrupted while waiting for EPUB {} to finish", file);
				return;
			}
		}
		if (uploadEpubFilesSingleton.isRunning() && uploadEpubFilesSingleton.getProcessedItems() < expectedProcessed) {
			uploadEpubFilesSingleton.addMoveError();
			log.error("Timed out waiting for EPUB {} to be moved", file);
		}
	}
}
