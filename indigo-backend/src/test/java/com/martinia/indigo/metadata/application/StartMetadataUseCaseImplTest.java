package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.MetadataProcessType;
import com.martinia.indigo.metadata.domain.model.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillReviewsMetadataCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartMetadataUseCaseImplTest {

	@Mock
	private CommandBus commandBus;

	@Mock
	private MetadataSingleton metadataSingleton;

	@InjectMocks
	private StartMetadataUseCaseImpl startMetadataUseCase;

	@Test
	void start_WhenFullProcessBooks_ShouldExecuteBooksCommand() {
		when(metadataSingleton.isRunning()).thenReturn(false);

		startMetadataUseCase.start("en", MetadataProcessType.FULL.name(), "BOOKS");

		verify(metadataSingleton).start(MetadataProcessType.FULL.name(), "BOOKS");
		verify(commandBus).execute(any(StartFillBooksMetadataCommand.class));
	}

	@Test
	void start_WhenFullProcessAuthors_ShouldExecuteAuthorsCommand() {
		when(metadataSingleton.isRunning()).thenReturn(false);

		startMetadataUseCase.start("es", MetadataProcessType.FULL.name(), "AUTHORS");

		verify(metadataSingleton).start(MetadataProcessType.FULL.name(), "AUTHORS");
		verify(commandBus).execute(any(StartFillAuthorsMetadataCommand.class));
	}

	@Test
	void start_WhenFullProcessReviews_ShouldExecuteReviewsCommand() {
		when(metadataSingleton.isRunning()).thenReturn(false);

		startMetadataUseCase.start("fr", MetadataProcessType.FULL.name(), "REVIEWS");

		verify(metadataSingleton).start(MetadataProcessType.FULL.name(), "REVIEWS");
		verify(commandBus).execute(any(StartFillReviewsMetadataCommand.class));
	}

	@Test
	void start_WhenPartialProcessBooks_ShouldExecuteBooksCommandWithoutOverride() {
		when(metadataSingleton.isRunning()).thenReturn(false);

		startMetadataUseCase.start("en", MetadataProcessType.PARTIAL.name(), "BOOKS");

		verify(metadataSingleton).start(MetadataProcessType.PARTIAL.name(), "BOOKS");
		verify(commandBus).execute(any(StartFillBooksMetadataCommand.class));
	}

	@Test
	void start_WhenPartialProcessAuthors_ShouldExecuteAuthorsCommandWithoutOverride() {
		when(metadataSingleton.isRunning()).thenReturn(false);

		startMetadataUseCase.start("de", MetadataProcessType.PARTIAL.name(), "AUTHORS");

		verify(metadataSingleton).start(MetadataProcessType.PARTIAL.name(), "AUTHORS");
		verify(commandBus).execute(any(StartFillAuthorsMetadataCommand.class));
	}

	@Test
	void start_WhenPartialProcessReviews_ShouldExecuteReviewsCommandWithOverride() {
		when(metadataSingleton.isRunning()).thenReturn(false);

		startMetadataUseCase.start("it", MetadataProcessType.PARTIAL.name(), "REVIEWS");

		verify(metadataSingleton).start(MetadataProcessType.PARTIAL.name(), "REVIEWS");
		verify(commandBus).execute(any(StartFillReviewsMetadataCommand.class));
	}

	@Test
	void start_WhenSingletonIsRunning_ShouldStopItFirst() {
		when(metadataSingleton.isRunning()).thenReturn(true);

		startMetadataUseCase.start("en", MetadataProcessType.FULL.name(), "BOOKS");

		verify(metadataSingleton).stop();
		verify(metadataSingleton).start(MetadataProcessType.FULL.name(), "BOOKS");
		verify(commandBus).execute(any(StartFillBooksMetadataCommand.class));
	}

	@Test
	void start_WhenUnknownEntity_ShouldNotExecuteCommand() {
		when(metadataSingleton.isRunning()).thenReturn(false);

		startMetadataUseCase.start("en", MetadataProcessType.FULL.name(), "UNKNOWN");

		verify(metadataSingleton).start(MetadataProcessType.FULL.name(), "UNKNOWN");
		verify(commandBus, never()).execute(any());
	}

	@Test
	void start_WhenUnknownProcessType_ShouldNotExecuteCommand() {
		when(metadataSingleton.isRunning()).thenReturn(false);

		startMetadataUseCase.start("en", "UNKNOWN", "BOOKS");

		verify(metadataSingleton).start("UNKNOWN", "BOOKS");
		verify(commandBus, never()).execute(any());
	}
}