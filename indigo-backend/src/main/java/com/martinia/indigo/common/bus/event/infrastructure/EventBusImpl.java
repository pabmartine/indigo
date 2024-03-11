package com.martinia.indigo.common.bus.event.infrastructure;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@Primary
public class EventBusImpl implements EventBus {

	@Resource
	private ApplicationEventMulticaster applicationEventMulticaster;

	@Resource
	private GenericApplicationContext applicationContext;

	public void publish(Event event) {
		applicationEventMulticaster.multicastEvent(new PayloadApplicationEvent<>(applicationContext, event));
	}
}