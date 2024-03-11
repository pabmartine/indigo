package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.FindReviewMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindReviewMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class FindReviewMetadataCommandHandlerTest extends BaseIndigoTest {

	@Mock
	private FindReviewMetadataUseCase findReviewMetadataUseCase;

	@InjectMocks
	@Resource
	private FindReviewMetadataCommandHandler findReviewMetadataCommandHandler;

	@Test
	public void testHandle() {
		// Given
		String bookId = "book-123";
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);
		String lang = "en";

		FindReviewMetadataCommand command = FindReviewMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
				.lastExecution(lastExecution)
				.lang(lang)
				.build();

		// When
		findReviewMetadataCommandHandler.handle(command);

		// Then
		verify(findReviewMetadataUseCase, times(1)).find(bookId, override, lastExecution, lang);
	}
}
