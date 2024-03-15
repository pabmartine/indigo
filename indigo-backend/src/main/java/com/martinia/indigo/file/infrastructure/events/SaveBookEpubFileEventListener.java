package com.martinia.indigo.file.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.file.domain.model.events.EpubFileExtractedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveBookEpubFileExtractedEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SaveBookEpubFileEventListener extends EventBusListener<EpubFileExtractedEvent> {

	@Resource
	private SaveBookEpubFileExtractedEventUseCase useCase;

	@Override
	public void handle(final EpubFileExtractedEvent event) {
		useCase.save(event.getBookOpf(), event.getPath());
	}
}