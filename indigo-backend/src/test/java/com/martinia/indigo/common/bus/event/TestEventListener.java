package com.martinia.indigo.common.bus.event;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestEventListener extends EventBusListener<TestEvent> {

	@Override
	public void handle(final TestEvent event) {
		log.info(event.getId());
	}
}
