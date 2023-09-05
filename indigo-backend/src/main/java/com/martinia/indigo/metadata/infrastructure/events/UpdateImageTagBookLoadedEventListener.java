package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.metadata.domain.model.events.BookLoadedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.UpdateImageTagMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class UpdateImageTagBookLoadedEventListener extends EventBusListener<BookLoadedEvent> {

	@Resource
	private UpdateImageTagMetadataUseCase updateImageTagMetadataUseCase;

	@Override
	public void handle(final BookLoadedEvent event) {
		updateImageTagMetadataUseCase.updateImage(event.getBookId());
	}
}