package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindAuthorMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class FindAuthorMetadataCommandHandler extends CommandHandler<FindAuthorMetadataCommand, MetadataItemResult> {

	@Resource
	private FindAuthorMetadataUseCase findAuthorMetadataUseCase;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.MetadataActivityService activity;

	@Override
	public MetadataItemResult handle(final FindAuthorMetadataCommand command) {
		if (activity != null) return activity.track("AUTHORS", command.getAuthorId(), command.getLang(), () ->
				findAuthorMetadataUseCase.find(command.getAuthorId(), command.isOverride(), command.getLastExecution(), command.getLang()));
		return findAuthorMetadataUseCase.find(command.getAuthorId(), command.isOverride(), command.getLastExecution(), command.getLang());
	}
}
