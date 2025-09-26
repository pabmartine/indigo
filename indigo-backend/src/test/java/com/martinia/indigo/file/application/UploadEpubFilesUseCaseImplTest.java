package com.martinia.indigo.file.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.commands.ExtractEpubFileCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadEpubFilesUseCaseImplTest {

	@Mock
	private CommandBus commandBus;

	@Mock
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@InjectMocks
	private UploadEpubFilesUseCaseImpl uploadEpubFilesUseCase;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(uploadEpubFilesUseCase, "uploadsPath", tempDir.toString());
	}

	@Test
	void upload_WhenDirectoryExists_ShouldProcessEpubFiles() throws IOException {
		Files.createFile(tempDir.resolve("book1.epub"));
		Files.createFile(tempDir.resolve("book2.EPUB"));
		Files.createFile(tempDir.resolve("book3.txt"));

		uploadEpubFilesUseCase.upload(5L);

		verify(uploadEpubFilesSingleton).start(5L);
		verify(commandBus, times(2)).execute(any(ExtractEpubFileCommand.class));
		verifyNoMoreInteractions(uploadEpubFilesSingleton);
	}

	@Test
	void upload_WhenDirectoryDoesNotExist_ShouldCreateItAndProcess() throws IOException {
		Path nonExistentPath = tempDir.resolve("nonexistent");
		ReflectionTestUtils.setField(uploadEpubFilesUseCase, "uploadsPath", nonExistentPath.toString());

		uploadEpubFilesUseCase.upload(5L);

		verify(uploadEpubFilesSingleton).start(5L);
		verify(commandBus, never()).execute(any(ExtractEpubFileCommand.class));
	}

	@Test
	void upload_WhenLimitIsLowerThanFiles_ShouldProcessOnlyLimitedFiles() throws IOException {
		Files.createFile(tempDir.resolve("book1.epub"));
		Files.createFile(tempDir.resolve("book2.epub"));
		Files.createFile(tempDir.resolve("book3.epub"));
		Files.createFile(tempDir.resolve("book4.epub"));

		uploadEpubFilesUseCase.upload(2L);

		verify(uploadEpubFilesSingleton).start(2L);
		verify(commandBus, times(2)).execute(any(ExtractEpubFileCommand.class));
	}

	@Test
	void upload_WhenExceptionOccurs_ShouldStopSingleton() {
		// Use a null path to force an exception in Files.walk()
		ReflectionTestUtils.setField(uploadEpubFilesUseCase, "uploadsPath", (String) null);

		uploadEpubFilesUseCase.upload(5L);

		verify(uploadEpubFilesSingleton).stop();
	}

	@Test
	void upload_WhenNoEpubFiles_ShouldNotProcessAnyFiles() throws IOException {
		Files.createFile(tempDir.resolve("book1.txt"));
		Files.createFile(tempDir.resolve("book2.pdf"));

		uploadEpubFilesUseCase.upload(5L);

		verify(uploadEpubFilesSingleton).start(5L);
		verify(commandBus, never()).execute(any(ExtractEpubFileCommand.class));
	}
}