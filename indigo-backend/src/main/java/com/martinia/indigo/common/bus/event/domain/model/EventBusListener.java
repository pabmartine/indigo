package com.martinia.indigo.common.bus.event.domain.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class EventBusListener<T> {

	protected abstract void handle(T t) throws Exception;

	@EventListener
	protected void listener(T t) throws Exception {
		if (t instanceof Event) {log.debug("Received {} event", t.getClass().getName());}
		this.handle(t);
	}
}