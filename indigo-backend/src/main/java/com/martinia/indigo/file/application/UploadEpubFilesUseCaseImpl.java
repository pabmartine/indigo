package com.martinia.indigo.file.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.commands.ExtractEpubFileCommand;
import com.martinia.indigo.file.domain.ports.usecases.UploadEpubFilesUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UploadEpubFilesUseCaseImpl implements UploadEpubFilesUseCase {

	@Resource
	private CountEpubFilesUseCaseImpl detectedFiles;

	@Resource
	private CommandBus commandBus;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private ParallelEpubImporter parallelImporter;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private PendingImportService pendingImports;

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
			List<Path> epubFiles = detectedFiles.takePaths(number);
			uploadEpubFilesSingleton.start(epubFiles.size());
			if (parallelImporter != null) uploadEpubFilesSingleton.beginManagedProcessing();
			CompletableFuture.runAsync(() -> processFiles(epubFiles));
		}
		catch (Exception e) {
			log.error("Could not process EPUB uploads", e);
			throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo iniciar la importación", e);
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
			detectedFiles.invalidate();
			if (parallelImporter != null) uploadEpubFilesSingleton.endManagedProcessing();
			else uploadEpubFilesSingleton.stop();
			if (pendingImports != null) pendingImports.resumeAfterBatch();
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
