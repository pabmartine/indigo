package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.metadata.application.commands.FindReviewMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindReviewMetadataUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FindSimilarBooksMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class BookMetadataFoundFindReviewsEventListener extends EventBusListener<BookMetadataFoundEvent> {

	@Resource
	private FindReviewMetadataUseCase findReviewMetadataUseCase;

	@Override
	protected void handle(final BookMetadataFoundEvent event) {
//		findReviewMetadataUseCase.find(event.getBookId(), true, "es");
	}

}
