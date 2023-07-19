package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.ports.commands.LoadBookCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.LoadBookCommandUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

class LoadBookCommandHandlerTest extends BaseIndigoTest {
	@MockBean
	private LoadBookCommandUseCase loadBookCommandUseCase;

	@Resource
	private LoadBookCommandHandler handler;

	@MockBean
	private CommandBus commandBus;

	@Test
	void handle_ShouldInvokeLoadBookCommandUseCase() {

		//Given
		String bookId = "12345";
		LoadBookCommand command = LoadBookCommand.builder().bookId(bookId).build();

		//When
		doNothing().when(loadBookCommandUseCase).load(bookId);

		// Then
		assertDoesNotThrow(() -> handler.handle(command));
		verify(loadBookCommandUseCase).load(bookId);
	}

}