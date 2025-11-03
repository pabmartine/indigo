package com.martinia.indigo.file.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FindEpubFilesUploadPathUseCaseImplTest {

	@InjectMocks
	private FindEpubFilesUploadPathUseCaseImpl findEpubFilesUploadPathUseCase;

	private static final String UPLOAD_PATH = "/test/upload/path";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(findEpubFilesUploadPathUseCase, "uploadsPath", UPLOAD_PATH);
	}

	@Test
	void findPath_ShouldReturnConfiguredUploadPath() {
		String result = findEpubFilesUploadPathUseCase.findPath();

		assertThat(result).isEqualTo(UPLOAD_PATH);
	}

	@Test
	void findPath_WhenUploadPathIsNull_ShouldReturnNull() {
		ReflectionTestUtils.setField(findEpubFilesUploadPathUseCase, "uploadsPath", null);

		String result = findEpubFilesUploadPathUseCase.findPath();

		assertThat(result).isNull();
	}

	@Test
	void findPath_WhenUploadPathIsEmpty_ShouldReturnEmptyString() {
		ReflectionTestUtils.setField(findEpubFilesUploadPathUseCase, "uploadsPath", "");

		String result = findEpubFilesUploadPathUseCase.findPath();

		assertThat(result).isEmpty();
	}
}