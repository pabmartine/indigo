package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.StartFillReviewsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillReviewsMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class StartFillReviewsMetadataCommandHandlerTest extends BaseIndigoTest {

	@MockBean
	private StartFillReviewsMetadataUseCase startFillReviewsMetadataUseCase;

	@Resource
	private StartFillReviewsMetadataCommandHandler startFillReviewsMetadataCommandHandler;

	@Test
	public void testHandle_Successful() {
		// Given
		boolean override = true;
		String lang = "en";

		// When
		startFillReviewsMetadataCommandHandler.handle(StartFillReviewsMetadataCommand.builder().override(override).lang(lang).build());

		// Then
		// Verify the method invocation
		verify(startFillReviewsMetadataUseCase, times(1)).start(override, lang);
	}
}
