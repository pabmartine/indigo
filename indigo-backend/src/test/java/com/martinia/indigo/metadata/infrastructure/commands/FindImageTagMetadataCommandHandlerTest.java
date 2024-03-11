package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.FindImageTagMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindImageTagMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class FindImageTagMetadataCommandHandlerTest extends BaseIndigoTest {

	@Resource
	private FindImageTagMetadataCommandHandler commandHandler;

	@MockBean
	private FindImageTagMetadataUseCase findImageTagMetadataUseCase;

	@Test
	public void testHandle() {
		// Given
		FindImageTagMetadataCommand command = FindImageTagMetadataCommand.builder().tag("tagToFind").build();
		String expectedMetadata = "metadataValue";

		when(findImageTagMetadataUseCase.find("tagToFind")).thenReturn(expectedMetadata);

		// When
		String result = commandHandler.handle(command);

		// Then
		assertNotNull(result);
		assertEquals(expectedMetadata, result);

		verify(findImageTagMetadataUseCase).find("tagToFind");
	}

}