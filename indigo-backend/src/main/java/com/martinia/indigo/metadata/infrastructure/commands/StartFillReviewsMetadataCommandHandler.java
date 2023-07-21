package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.ports.commands.StartFillReviewsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillReviewsMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class StartFillReviewsMetadataCommandHandler implements CommandHandler<StartFillReviewsMetadataCommand, Void> {

	@Resource
	private StartFillReviewsMetadataUseCase startFillReviewsMetadataUseCase;

	@Override
	public Void handle(final StartFillReviewsMetadataCommand command) {

		startFillReviewsMetadataUseCase.start(command.isOverride(), command.getLang());

		return null;
	}
}
