package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.FindImageTagMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindImageTagMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class FindImageTagMetadataCommandHandler implements CommandHandler<FindImageTagMetadataCommand, String> {

	@Resource
	private FindImageTagMetadataUseCase findImageTagMetadataUseCase;

	@Override
	public String handle(final FindImageTagMetadataCommand command) {
		return findImageTagMetadataUseCase.find(command.getTag());
	}
}
