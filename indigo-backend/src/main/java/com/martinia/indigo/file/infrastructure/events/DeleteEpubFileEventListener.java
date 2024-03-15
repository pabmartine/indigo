package com.martinia.indigo.file.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.file.domain.model.events.FileMovedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.DeleteEpubFileEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DeleteEpubFileEventListener extends EventBusListener<FileMovedEvent> {

	@Resource
	private DeleteEpubFileEventUseCase deleteEpubFileEventUseCase;

	@Override
	public void handle(final FileMovedEvent event) {
		deleteEpubFileEventUseCase.delete(event.getSourcePath());
	}
}