package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.events.InitialLoadStartedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.InitialLoadStartedEventUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;

class InitialLoadStartedEventListenerTest extends BaseIndigoTest {

	@MockBean
	private InitialLoadStartedEventUseCase initialLoadStartedEventUseCase;

	@Resource
	private InitialLoadStartedEventListener listener;

	@Test
	@Transactional
	void handle_ShouldInvokeInitialLoadStartedEventUseCase() {
		// Given
		InitialLoadStartedEvent event = InitialLoadStartedEvent.builder().build();

		// When
		listener.handle(event);

		// Then
		verify(initialLoadStartedEventUseCase).start();
	}

}