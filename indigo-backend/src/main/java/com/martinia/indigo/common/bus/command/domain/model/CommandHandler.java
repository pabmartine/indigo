package com.martinia.indigo.common.bus.command.domain.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import javax.transaction.Transactional;
import java.lang.reflect.ParameterizedType;

@Slf4j
public abstract class CommandHandler<T extends Command<R>, R> {

	@Retryable(maxAttemptsExpression = "#{${retries.maxAttempts.commands:3}}", backoff = @Backoff(delayExpression = "#{${retries.delay.commands:1000}}", multiplierExpression = "#{${retries.multiplier.commands:2}}", maxDelayExpression = "#{${retries.maxDelay.commands:0}}"))
	public abstract R handle(T command) throws Exception;

	@Recover
	@Transactional
	public void handleOnError(final Throwable exception, final R command) {
		log.error("Retries for command {} exhausted", command.getClass().getSimpleName(), exception);
	}

	public Class<T> getCommandClass() {
		ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
		return (Class<T>) superclass.getActualTypeArguments()[0];
	}
}