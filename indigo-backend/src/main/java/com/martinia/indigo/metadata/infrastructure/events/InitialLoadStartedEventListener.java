package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.metadata.domain.ports.events.InitialLoadStartedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.InitialLoadStartedEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class InitialLoadStartedEventListener extends EventBusListener<InitialLoadStartedEvent> {

	@Resource
	private InitialLoadStartedEventUseCase initialLoadStartedEventUseCase;

	@Override
	public void handle(final InitialLoadStartedEvent event) {
		initialLoadStartedEventUseCase.start();
	}
}