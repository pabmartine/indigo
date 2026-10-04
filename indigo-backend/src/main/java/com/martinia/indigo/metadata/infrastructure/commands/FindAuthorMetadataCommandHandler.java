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
    @org.springframework.retry.annotation.Retryable(
            noRetryFor = java.util.concurrent.CancellationException.class,
            notRecoverable = java.util.concurrent.CancellationException.class,
            maxAttemptsExpression = "#{${retries.maxAttempts.commands:3}}",
            backoff = @org.springframework.retry.annotation.Backoff(delayExpression = "#{${retries.delay.commands:1000}}",
                    multiplierExpression = "#{${retries.multiplier.commands:2}}", maxDelayExpression = "#{${retries.maxDelay.commands:0}}"))
	public MetadataItemResult handle(final FindAuthorMetadataCommand command) {
		if (activity != null) return activity.track("AUTHORS", command.getAuthorId(), command.getLang(), () ->
				find(command));
		return find(command);
	}
	private MetadataItemResult find(FindAuthorMetadataCommand command) {
		return command.getRunId() > 0
				? findAuthorMetadataUseCase.find(command.getAuthorId(), command.isOverride(), command.getLastExecution(), command.getLang(), command.getRunId())
				: findAuthorMetadataUseCase.find(command.getAuthorId(), command.isOverride(), command.getLastExecution(), command.getLang());
	}
}
