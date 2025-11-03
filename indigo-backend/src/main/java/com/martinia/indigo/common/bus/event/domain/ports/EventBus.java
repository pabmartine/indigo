package com.martinia.indigo.common.bus.event.domain.ports;

import com.martinia.indigo.common.bus.event.domain.model.Event;

public interface EventBus {

	void publish(Event e);

}