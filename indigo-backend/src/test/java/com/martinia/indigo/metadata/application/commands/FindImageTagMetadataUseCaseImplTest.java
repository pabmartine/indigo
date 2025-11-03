package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.application.commands.FindImageTagMetadataUseCase.FindImageTagMetadataCommand;
import com.martinia.indigo.metadata.application.commands.FindImageTagMetadataUseCase.FindImageTagMetadataResult;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class FindImageTagMetadataUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindImageTagMetadataUseCaseImpl useCase;

	@Test
	public void testFind() {
		// Given
		String imagePath = "testImagePath";

		// When
		FindImageTagMetadataCommand command = new FindImageTagMetadataCommand(imagePath);
		FindImageTagMetadataResult result = useCase.findImageTagMetadata(command);

		// Then
		assertNotNull(result.getImageTagMetadata());
		assertEquals("unknown", result.getImageTagMetadata().getFormat());
		assertEquals(0, result.getImageTagMetadata().getWidth());
		assertEquals(0, result.getImageTagMetadata().getHeight());
		assertEquals(0, result.getImageTagMetadata().getTags().size());

	}

}