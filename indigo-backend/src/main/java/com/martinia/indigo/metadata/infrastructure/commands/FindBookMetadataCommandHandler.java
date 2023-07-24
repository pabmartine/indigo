package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindBookMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class FindBookMetadataCommandHandler implements CommandHandler<FindBookMetadataCommand, Void> {

	@Resource
	private FindBookMetadataUseCase findBookMetadataUseCase;

	@Override
	public Void handle(final FindBookMetadataCommand command) {

		findBookMetadataUseCase.find(command.getBookId(), command.isOverride(), command.getLastExecution());

		return null;
	}
}
