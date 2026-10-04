package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.metadata.domain.model.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillAuthorsMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class StartFillAuthorsMetadataCommandHandler extends CommandHandler<StartFillAuthorsMetadataCommand, Void> {

	@Resource
	private StartFillAuthorsMetadataUseCase startFillAuthorsMetadataUseCase;

	@Override
    @org.springframework.retry.annotation.Retryable(
            noRetryFor = java.util.concurrent.CancellationException.class,
            notRecoverable = java.util.concurrent.CancellationException.class,
            maxAttemptsExpression = "#{${retries.maxAttempts.commands:3}}",
            backoff = @org.springframework.retry.annotation.Backoff(delayExpression = "#{${retries.delay.commands:1000}}",
                    multiplierExpression = "#{${retries.multiplier.commands:2}}", maxDelayExpression = "#{${retries.maxDelay.commands:0}}"))
	public Void handle(final StartFillAuthorsMetadataCommand command) {

		startFillAuthorsMetadataUseCase.start(command.isOverride(), command.getLang(), command.getRunId());

		return null;
	}
}
