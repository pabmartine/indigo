package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.ports.commands.LoadBookCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.LoadBookCommandUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class LoadBookCommandHandler implements CommandHandler<LoadBookCommand, Void> {

	@Resource
	private LoadBookCommandUseCase loadBookCommandUseCase;

	@Override
	public Void handle(final LoadBookCommand command) throws Exception {
		loadBookCommandUseCase.load(command.getBookId());
		return null;
	}
}
