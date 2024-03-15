package com.martinia.indigo.file.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveTagEpubFileEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SaveTagEpubFileEventListener extends EventBusListener<EpubFileAddedEvent> {

	@Resource
	private SaveTagEpubFileEventUseCase useCase;

	@Override
	public void handle(final EpubFileAddedEvent event) {
		useCase.save(event.getBookId());
	}
}