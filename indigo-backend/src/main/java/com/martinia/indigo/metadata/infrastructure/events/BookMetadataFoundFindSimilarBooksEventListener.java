package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.metadata.domain.ports.events.BookMetadataFoundEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FindSimilarBooksMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class BookMetadataFoundFindSimilarBooksEventListener extends EventBusListener<BookMetadataFoundEvent> {

	@Resource
	private FindSimilarBooksMetadataUseCase findSimilarBooksMetadataUseCase;

	@Override
	protected void handle(final BookMetadataFoundEvent event) {
		findSimilarBooksMetadataUseCase.findSimilarBooks(event.getBookId(), event.getSimilar());
	}

}
