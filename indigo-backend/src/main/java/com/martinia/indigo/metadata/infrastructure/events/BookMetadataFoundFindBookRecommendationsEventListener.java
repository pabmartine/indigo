package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FindBookRecommendationsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class BookMetadataFoundFindBookRecommendationsEventListener extends EventBusListener<BookMetadataFoundEvent> {

	@Resource
	private FindBookRecommendationsUseCase findBookRecommendationsUseCase;

	@Override
	protected void handle(final BookMetadataFoundEvent event) {
		findBookRecommendationsUseCase.findBookRecommendations(event.getBookId());
	}

}
