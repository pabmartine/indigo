package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.metadata.domain.ports.events.BookLoadedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.BookLoadedEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Slf4j
@Component
public class BookLoadedEventListener extends EventBusListener<BookLoadedEvent> {

	@Resource
	private BookLoadedEventUseCase bookLoadedEventUseCase;

	@Override
	public void handle(final BookLoadedEvent event) {
		bookLoadedEventUseCase.fillAuthors(event.getBookId());
	}
}