package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillBooksMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class StartFillBooksMetadataCommandHandler implements CommandHandler<StartFillBooksMetadataCommand, Void> {

	@Resource
	private StartFillBooksMetadataUseCase startFillBooksMetadataUseCase;

	@Override
	public Void handle(final StartFillBooksMetadataCommand command) {

		startFillBooksMetadataUseCase.start(command.isOverride());

		return null;
	}
}
