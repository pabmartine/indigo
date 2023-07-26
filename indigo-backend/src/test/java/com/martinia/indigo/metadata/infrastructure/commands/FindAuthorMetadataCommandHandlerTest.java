package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindAuthorMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class FindAuthorMetadataCommandHandlerTest extends BaseIndigoTest {

	@MockBean
	private FindAuthorMetadataUseCase findAuthorMetadataUseCase;

	@Resource
	private FindAuthorMetadataCommandHandler findAuthorMetadataCommandHandler;

	@Test
	public void testHandle() {
		// Given
		String authorId = "author-123";
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);
		String lang = "es";

		FindAuthorMetadataCommand command = FindAuthorMetadataCommand.builder()
				.authorId(authorId)
				.override(override)
				.lastExecution(lastExecution)
				.lang(lang)
				.build();

		// When
		findAuthorMetadataCommandHandler.handle(command);

		// Then
		verify(findAuthorMetadataUseCase, times(1)).find(authorId, override, lastExecution, lang);
	}
}
