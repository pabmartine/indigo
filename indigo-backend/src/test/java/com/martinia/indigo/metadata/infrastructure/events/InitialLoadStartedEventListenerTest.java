package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.events.InitialLoadStartedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.LoadBooksUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class InitialLoadStartedEventListenerTest extends BaseIndigoTest {

	@MockBean
	private LoadBooksUseCase loadBooksUseCase;

	@Resource
	private InitialLoadStartedEventListener initialLoadStartedEventListener;

	@Test
	public void testHandle() {
		// Given
		boolean override = true;
		InitialLoadStartedEvent event = InitialLoadStartedEvent.builder().override(override).build();

		// When
		initialLoadStartedEventListener.handle(event);

		// Then
		verify(loadBooksUseCase, times(1)).start(override);
	}
}
