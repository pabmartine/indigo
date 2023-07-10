package com.martinia.indigo.book.application.cover;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.cover.FindBookCoverByPathUseCase;
import com.martinia.indigo.common.util.UtilComponent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class FindBookCoverByPathUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookCoverByPathUseCase findBookCoverByPathUseCase;

	@MockBean
	private UtilComponent utilComponent;

	@Test
	public void testGetImage_WithValidPath() {
		// Given
		String path = "valid/path";
		String base64Image = "base64encodedimage";

		// Mock the behavior of utilComponent.getBase64Cover()
		when(utilComponent.getBase64Cover(path, false)).thenReturn(base64Image);

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
		when(utilComponent.getBase64Cover(path, false)).thenReturn(null);

		// When
		Optional<String> imageOptional = findBookCoverByPathUseCase.getImage(path);

		// Then
		assertFalse(imageOptional.isPresent());
	}
}
