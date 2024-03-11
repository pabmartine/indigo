package com.martinia.indigo.file.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.file.domain.model.commands.ExtractEpubFileCommand;
import com.martinia.indigo.file.domain.ports.usecases.commands.ExtractEpubFileUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ExtractEpubFileCommandHandler extends CommandHandler<ExtractEpubFileCommand, Void> {

	@Resource
	private ExtractEpubFileUseCase extractEpubFileUseCase;

	@Override
	public Void handle(final ExtractEpubFileCommand command) {

		extractEpubFileUseCase.extract(command.getFile());

		return null;
	}
}
