package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FindBookRecommendationsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class BookMetadataFoundFindBookRecommendationsEventListenerTest extends BaseIndigoTest {

	@MockBean
	private FindBookRecommendationsUseCase findBookRecommendationsUseCase;

	@Resource
	private BookMetadataFoundFindBookRecommendationsEventListener bookMetadataFoundFindBookRecommendationsEventListener;

	@Test
	public void testHandle() {
		// Given
		String bookId = "12345";
		BookMetadataFoundEvent event = BookMetadataFoundEvent.builder().bookId(bookId).build();

		// When
		bookMetadataFoundFindBookRecommendationsEventListener.handle(event);

		// Then
		verify(findBookRecommendationsUseCase, times(1)).findBookRecommendations(bookId);
	}
}
