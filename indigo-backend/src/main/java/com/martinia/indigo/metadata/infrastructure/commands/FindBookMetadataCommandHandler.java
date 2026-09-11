package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindBookMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class FindBookMetadataCommandHandler extends CommandHandler<FindBookMetadataCommand, MetadataItemResult> {

	@Resource
	private FindBookMetadataUseCase findBookMetadataUseCase;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.MetadataActivityService activity;

	@Override
	public MetadataItemResult handle(final FindBookMetadataCommand command) {
		if (activity != null) return activity.track("BOOKS", command.getBookId(), "es", () ->
				findBookMetadataUseCase.find(command.getBookId(), command.getMergePolicy(), command.getDynamicPolicy(), command.getLastExecution()));
		return findBookMetadataUseCase.find(command.getBookId(), command.getMergePolicy(), command.getDynamicPolicy(),
				command.getLastExecution());
	}
}
