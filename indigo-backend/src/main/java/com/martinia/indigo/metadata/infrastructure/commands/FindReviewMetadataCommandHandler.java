package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.FindReviewMetadataCommand;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindReviewMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class FindReviewMetadataCommandHandler extends CommandHandler<FindReviewMetadataCommand, MetadataItemResult> {

	@Resource
	private FindReviewMetadataUseCase findReviewMetadataUseCase;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.MetadataActivityService activity;

	@Override
	public MetadataItemResult handle(final FindReviewMetadataCommand command) {
		if (activity != null) return activity.track("REVIEWS", command.getBookId(), command.getLang(), () ->
				findReviewMetadataUseCase.find(command.getBookId(), command.isOverride(), command.getLang()));
		return findReviewMetadataUseCase.find(command.getBookId(), command.isOverride(), command.getLang());
	}
}
