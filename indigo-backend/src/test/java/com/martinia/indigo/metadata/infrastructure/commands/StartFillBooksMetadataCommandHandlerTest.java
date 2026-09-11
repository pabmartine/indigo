package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.BookMetadataScope;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillBooksMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;

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
		// When
		startFillBooksMetadataCommandHandler.handle(StartFillBooksMetadataCommand.builder()
				.scope(BookMetadataScope.ALL)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.build());

		// Then
		// Verify the method invocation
		verify(startFillBooksMetadataUseCase, times(1)).start(BookMetadataScope.ALL,
				MetadataMergePolicy.FILL_MISSING, DynamicMetadataPolicy.REFRESH_IF_STALE, 0);
	}
}
