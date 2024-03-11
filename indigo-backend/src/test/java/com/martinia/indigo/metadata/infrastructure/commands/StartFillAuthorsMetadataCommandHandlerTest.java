package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillAuthorsMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class StartFillAuthorsMetadataCommandHandlerTest extends BaseIndigoTest {

	@MockBean
	private StartFillAuthorsMetadataUseCase startFillAuthorsMetadataUseCase;

	@Resource
	private StartFillAuthorsMetadataCommandHandler startFillAuthorsMetadataCommandHandler;

	@Test
	public void testHandle_Successful() {
		// Given
		boolean override = true;
		String lang = "en";

		// When
		startFillAuthorsMetadataCommandHandler.handle(StartFillAuthorsMetadataCommand.builder().override(override).lang(lang).build());

		// Then
		// Verify the method invocation
		verify(startFillAuthorsMetadataUseCase, times(1)).start(override, lang);
	}
}
