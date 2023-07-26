package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindBookMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class FindBookMetadataCommandHandlerTest extends BaseIndigoTest {

	@MockBean
	private FindBookMetadataUseCase findBookMetadataUseCase;

	@Resource
	private FindBookMetadataCommandHandler findBookMetadataCommandHandler;

	@Test
	public void testHandle() {
		// Given
		String bookId = "book-123";
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
				.lastExecution(lastExecution)
				.build();

		// When
		findBookMetadataCommandHandler.handle(command);

		// Then
		verify(findBookMetadataUseCase, times(1)).find(bookId, override, lastExecution);
	}
}
