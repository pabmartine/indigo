package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.ports.commands.StartInitialLoadCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartInitialLoadCommandUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;

class StartInitialLoadCommandHandlerTest extends BaseIndigoTest {

	@MockBean
	private StartInitialLoadCommandUseCase startInitialLoadCommandUseCase;

	@Resource
	private StartInitialLoadCommandHandler handler;

	@MockBean
	private CommandBus commandBus;

	@Test
	@Transactional
	void handle_ShouldInvokeStartInitialLoadCommandUseCase() {
		//When
		handler.handle(StartInitialLoadCommand.builder().build());

		// Then
		verify(startInitialLoadCommandUseCase).start();
	}

}