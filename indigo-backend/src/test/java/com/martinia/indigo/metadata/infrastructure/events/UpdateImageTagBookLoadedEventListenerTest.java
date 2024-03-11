package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.events.BookLoadedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.UpdateImageTagMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;

class UpdateImageTagBookLoadedEventListenerTest extends BaseIndigoTest {

	@Resource
	private UpdateImageTagBookLoadedEventListener eventListener;

	@MockBean
	private UpdateImageTagMetadataUseCase updateImageTagMetadataUseCase;

	@Test
	public void testHandle() {
		// Given
		BookLoadedEvent mockEvent = BookLoadedEvent.builder().bookId("testBookId").build();

		// When
		eventListener.handle(mockEvent);

		// Then
		verify(updateImageTagMetadataUseCase).updateImage("testBookId");
	}
}