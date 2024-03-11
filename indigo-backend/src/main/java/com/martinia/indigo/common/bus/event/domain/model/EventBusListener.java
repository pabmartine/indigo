package com.martinia.indigo.common.bus.event.domain.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Slf4j
@Component
public abstract class EventBusListener<T> {

	@Retryable(maxAttemptsExpression = "#{${retry.maxAttempts.events:3}}", backoff = @Backoff(delayExpression = "#{${retries.delay.events:1000}}", multiplierExpression = "#{${retries.multiplier.events:2}}", maxDelayExpression = "#{${retries.maxDelay.events:0}}"))
	protected abstract void handle(T t) throws Exception;

	@Recover
	@Transactional
	public void handleOnError(final Throwable exception, final T event) {
		log.error("Retries for event {} exhausted", event.getClass().getSimpleName(), exception);
	}

	@EventListener
	protected void listener(T t) throws Exception {
		this.handle(t);
	}

}