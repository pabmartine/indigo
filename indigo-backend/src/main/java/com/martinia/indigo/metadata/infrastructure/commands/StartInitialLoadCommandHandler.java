package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.ports.commands.StartInitialLoadCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartInitialLoadCommandUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class StartInitialLoadCommandHandler implements CommandHandler<StartInitialLoadCommand, Void> {

	@Resource
	private StartInitialLoadCommandUseCase startInitialLoadCommandUseCase;

	@Override
	public Void handle(final StartInitialLoadCommand command) {

		startInitialLoadCommandUseCase.start();

		return null;
	}
}
