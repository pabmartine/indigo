package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.events.BookLoadedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FillAuthorsMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class BookLoadedEventListenerTest extends BaseIndigoTest {

	@MockBean
	private FillAuthorsMetadataUseCase fillAuthorsMetadataUseCase;

	@Resource
	private BookLoadedEventListener bookLoadedEventListener;

	@Test
	public void testHandle() {
		// Given
		String bookId = "12345";
		BookLoadedEvent event = BookLoadedEvent.builder().bookId(bookId).build();

		// When
		bookLoadedEventListener.handle(event);

		// Then
		verify(fillAuthorsMetadataUseCase, times(1)).fillAuthors(bookId);
	}
}
