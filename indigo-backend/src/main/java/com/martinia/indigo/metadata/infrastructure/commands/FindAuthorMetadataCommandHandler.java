package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindAuthorMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class FindAuthorMetadataCommandHandler implements CommandHandler<FindAuthorMetadataCommand, Void> {

	@Resource
	private FindAuthorMetadataUseCase findAuthorMetadataUseCase;

	@Override
	public Void handle(final FindAuthorMetadataCommand command) {

		findAuthorMetadataUseCase.find(command.getAuthorId(), command.isOverride(), command.getLastExecution(), command.getLang());

		return null;
	}
}
