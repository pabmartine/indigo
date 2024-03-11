package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillAuthorsMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class StartFillAuthorsMetadataCommandHandler extends CommandHandler<StartFillAuthorsMetadataCommand, Void> {

	@Resource
	private StartFillAuthorsMetadataUseCase startFillAuthorsMetadataUseCase;

	@Override
	public Void handle(final StartFillAuthorsMetadataCommand command) {

		startFillAuthorsMetadataUseCase.start(command.isOverride(), command.getLang());

		return null;
	}
}
