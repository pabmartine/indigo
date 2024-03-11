package com.martinia.indigo.common.bus.event.infrastructure;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@Primary
public class EventBusImpl implements EventBus {

	@Resource
	private ApplicationEventPublisher applicationEventPublisher;

	public void publish(Event event) {
//		log.debug("Published {} event", event.getClass().getName());
		applicationEventPublisher.publishEvent(event);
	}
}