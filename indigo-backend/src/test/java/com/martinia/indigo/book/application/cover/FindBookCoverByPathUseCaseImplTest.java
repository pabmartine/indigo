package com.martinia.indigo.book.application.cover;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.cover.FindBookCoverByPathUseCase;
import com.martinia.indigo.common.util.ImageUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class FindBookCoverByPathUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookCoverByPathUseCase findBookCoverByPathUseCase;

	@MockBean
	private ImageUtils imageUtils;

	@Test
	public void testGetImage_WithValidPath() {
		// Given
		String path = "valid/path";
		String base64Image = "base64encodedimage";

		// Mock the behavior of utilComponent.getBase64Cover()
		when(imageUtils.getBase64Cover(path, true)).thenReturn(base64Image);

		// When
		Optional<String> imageOptional = findBookCoverByPathUseCase.getImage(path);

		// Then
		assertTrue(imageOptional.isPresent());
		assertEquals(base64Image, imageOptional.get());
	}

	@Test
	public void testGetImage_WithInvalidPath() {
		// Given
		String path = "invalid/path";

		// Mock the behavior of utilComponent.getBase64Cover()
		when(imageUtils.getBase64Cover(path, true)).thenReturn(null);

		// When
		Optional<String> imageOptional = findBookCoverByPathUseCase.getImage(path);

		// Then
		assertFalse(imageOptional.isPresent());
	}

	@Test
	public void testGetImage_WithNullPath() {
		// Given
		String nullPath = null;

		// When
		Optional<String> imageOptional = findBookCoverByPathUseCase.getImage(nullPath);

		// Then
		assertFalse(imageOptional.isPresent());
	}

	@Test
	public void testGetImage_WithEmptyPath() {
		// Given
		String emptyPath = "";

		// When
		Optional<String> imageOptional = findBookCoverByPathUseCase.getImage(emptyPath);

		// Then
		assertFalse(imageOptional.isPresent());
	}

	@Test
	public void testGetImage_WithAbsolutePath() {
		// Given
		String absolutePath = "/home/user/books/cover.jpg";
		String base64Image = "absolutePathImageBase64";

		when(imageUtils.getBase64Cover(absolutePath, true)).thenReturn(base64Image);

		// When
		Optional<String> imageOptional = findBookCoverByPathUseCase.getImage(absolutePath);

		// Then
		assertTrue(imageOptional.isPresent());
		assertEquals(base64Image, imageOptional.get());
	}

	@Test
	public void testGetImage_WithRelativePath() {
		// Given
		String relativePath = "books/fiction/cover.png";
		String base64Image = "relativePathImageBase64";

		when(imageUtils.getBase64Cover(relativePath, true)).thenReturn(base64Image);

		// When
		Optional<String> imageOptional = findBookCoverByPathUseCase.getImage(relativePath);

		// Then
		assertTrue(imageOptional.isPresent());
		assertEquals(base64Image, imageOptional.get());
	}
}
