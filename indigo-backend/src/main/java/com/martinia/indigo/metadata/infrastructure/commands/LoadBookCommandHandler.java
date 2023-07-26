package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.LoadBookCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.LoadBookUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class LoadBookCommandHandler implements CommandHandler<LoadBookCommand, Void> {

	@Resource
	private LoadBookUseCase loadBookUseCase;

	@Override
	public Void handle(final LoadBookCommand command) throws Exception {
		loadBookUseCase.load(command.getBookId(), command.isOverride());
		return null;
	}
}
