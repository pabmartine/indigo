package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.events.BookLoadedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.BookLoadedEventUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;

class BookLoadedEventListenerTest extends BaseIndigoTest {
	@MockBean
	private BookLoadedEventUseCase bookLoadedEventUseCase;

	@Resource
	private BookLoadedEventListener listener;

	@Test
	@Transactional
	void handle_ShouldInvokeBookLoadedEventUseCase() {
		// Given
		BookLoadedEvent event = BookLoadedEvent.builder().bookId("12345").build();

		// When
		listener.handle(event);

		// Then
		verify(bookLoadedEventUseCase).fillAuthors("12345");
	}
}