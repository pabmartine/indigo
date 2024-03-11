package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.FindReviewMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindReviewMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class FindReviewMetadataCommandHandler implements CommandHandler<FindReviewMetadataCommand, Void> {

	@Resource
	private FindReviewMetadataUseCase findReviewMetadataUseCase;

	@Override
	public Void handle(final FindReviewMetadataCommand command) {

		findReviewMetadataUseCase.find(command.getBookId(), command.isOverride(), command.getLastExecution(), command.getLang());

		return null;
	}
}
