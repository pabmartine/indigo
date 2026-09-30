package com.martinia.indigo.file.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CountEpubFilesUseCaseImplTest {

	@InjectMocks
	private CountEpubFilesUseCaseImpl countEpubFilesUseCase;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(countEpubFilesUseCase, "uploadsPath", tempDir.toString());
	}

	@Test
	void count_WhenDirectoryExists_ShouldCountEpubFiles() throws IOException {
		Files.createFile(tempDir.resolve("book1.epub"));
		Files.createFile(tempDir.resolve("book2.EPUB"));
		Files.createFile(tempDir.resolve("book3.txt"));
		Files.createFile(tempDir.resolve("book4.pdf"));

		Long result = countEpubFilesUseCase.count();

		assertThat(result).isEqualTo(2L);
	}

	@Test
	void count_WhenDirectoryExistsWithSubdirectories_ShouldCountAllEpubFiles() throws IOException {
		Files.createFile(tempDir.resolve("book1.epub"));

		Path subDir = tempDir.resolve("subdir");
		Files.createDirectory(subDir);
		Files.createFile(subDir.resolve("book2.epub"));
		Files.createFile(subDir.resolve("book3.txt"));

		Path deepSubDir = subDir.resolve("deep");
		Files.createDirectory(deepSubDir);
		Files.createFile(deepSubDir.resolve("book4.EPUB"));

		Long result = countEpubFilesUseCase.count();

		assertThat(result).isEqualTo(3L);
	}

	@Test
	void count_WhenDirectoryDoesNotExist_ShouldCreateItAndReturnZero() {
		Path nonExistentPath = tempDir.resolve("nonexistent");
		ReflectionTestUtils.setField(countEpubFilesUseCase, "uploadsPath", nonExistentPath.toString());

		Long result = countEpubFilesUseCase.count();

		assertThat(result).isEqualTo(0L);
		assertThat(Files.exists(nonExistentPath)).isTrue();
	}

	@Test
	void count_WhenDirectoryIsEmpty_ShouldReturnZero() {
		Long result = countEpubFilesUseCase.count();

		assertThat(result).isEqualTo(0L);
	}

	@Test
	void unreadableDirectoryMustNotMasqueradeAsAnEmptyUploadFolder() throws IOException {
		Path file = Files.createFile(tempDir.resolve("not-a-directory"));
		ReflectionTestUtils.setField(countEpubFilesUseCase, "uploadsPath", file.toString());
		org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
				() -> countEpubFilesUseCase.count());
	}

	@Test
	void acceptingDetectionReusesTheExactSelectionWithoutRescanning() throws IOException {
		Path first = Files.createFile(tempDir.resolve("first.epub"));
		assertThat(countEpubFilesUseCase.count()).isEqualTo(1);
		Files.createFile(tempDir.resolve("added-after-detection.epub"));
		assertThat(countEpubFilesUseCase.takePaths(10)).containsExactly(first);
		assertThat(countEpubFilesUseCase.count()).isEqualTo(2);
	}

	@Test
	void selectionExpiresAndBatchInvalidationRefreshesDetection() throws IOException {
		Files.createFile(tempDir.resolve("first.epub"));
		countEpubFilesUseCase.count();
		Files.createFile(tempDir.resolve("second.epub"));
		ReflectionTestUtils.setField(countEpubFilesUseCase, "completedAt", System.nanoTime() - java.time.Duration.ofMinutes(11).toNanos());
		assertThat(countEpubFilesUseCase.takePaths(10)).hasSize(2);
		assertThat(countEpubFilesUseCase.count()).isEqualTo(2);
		Files.createFile(tempDir.resolve("third.epub"));
		countEpubFilesUseCase.invalidate();
		assertThat(countEpubFilesUseCase.count()).isEqualTo(3);
	}

	@Test
	void count_WithMixedCaseExtensions_ShouldCountAllEpubFiles() throws IOException {
		Files.createFile(tempDir.resolve("book1.epub"));
		Files.createFile(tempDir.resolve("book2.EPUB"));
		Files.createFile(tempDir.resolve("book3.Epub"));
		Files.createFile(tempDir.resolve("book4.ePub"));
		Files.createFile(tempDir.resolve("book5.EPub"));

		Long result = countEpubFilesUseCase.count();

		assertThat(result).isEqualTo(5L);
	}
}