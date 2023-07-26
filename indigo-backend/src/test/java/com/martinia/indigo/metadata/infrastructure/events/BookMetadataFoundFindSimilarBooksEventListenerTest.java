package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FindSimilarBooksMetadataUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class BookMetadataFoundFindSimilarBooksEventListenerTest extends BaseIndigoTest {

	@MockBean
	private FindSimilarBooksMetadataUseCase findSimilarBooksMetadataUseCase;

	@Resource
	private BookMetadataFoundFindSimilarBooksEventListener bookMetadataFoundFindSimilarBooksEventListener;

	@Test
	public void testHandle() {
		// Given
		String bookId = "12345";
		String similar = "56789";
		BookMetadataFoundEvent event = BookMetadataFoundEvent.builder().bookId(bookId).similar(similar).build();

		// When
		bookMetadataFoundFindSimilarBooksEventListener.handle(event);

		// Then
		verify(findSimilarBooksMetadataUseCase, times(1)).findSimilarBooks(bookId, similar);
	}
}
