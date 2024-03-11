package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillBooksMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class StartFillBooksMetadataCommandHandlerTest extends BaseIndigoTest {

	@MockBean
	private StartFillBooksMetadataUseCase startFillBooksMetadataUseCase;

	@Resource
	private StartFillBooksMetadataCommandHandler startFillBooksMetadataCommandHandler;

	@Test
	public void testHandle_Successful() {
		// Given
		boolean override = true;

		// When
		startFillBooksMetadataCommandHandler.handle(StartFillBooksMetadataCommand.builder().override(override).build());

		// Then
		// Verify the method invocation
		verify(startFillBooksMetadataUseCase, times(1)).start(override);
	}
}
