package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.metadata.domain.ports.adapters.amazon.FindAmazonReviewsPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsReviewsPort;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class FindReviewMetadataFailureTest {
	@Test
	void retainsExistingReviewsAndDoesNotMarkRefreshSuccessfulWhenFallbackIsRestricted() {
		var books = mock(BookRepository.class);
		var goodreads = mock(FindGoodReadsReviewsPort.class);
		var amazon = mock(FindAmazonReviewsPort.class);
		var service = new FindReviewMetadataUseCaseImpl();
		ReflectionTestUtils.setField(service, "bookRepository", books);
		ReflectionTestUtils.setField(service, "findGoodReadsReviewsPort", Optional.of(goodreads));
		ReflectionTestUtils.setField(service, "findAmazonReviewsPort", Optional.of(amazon));
		List<ReviewMongo> existing = List.of(new ReviewMongo());
		var book = BookMongoEntity.builder().id("book").title("Book").authors(List.of("Author")).reviews(existing).build();
		when(books.findById("book")).thenReturn(Optional.of(book));
		when(goodreads.getReviews(anyString(), anyString(), anyList())).thenReturn(List.of());
		when(amazon.getReviews(anyString(), anyList())).thenThrow(new IllegalStateException("Authentication required"));
		assertEquals(MetadataItemResult.ERROR, service.find("book", true, "es"));
		assertSame(existing, book.getReviews());
		assertNull(book.getLastReviewsMetadataSync());
		assertTrue(book.getReviewsMetadataError().contains("AMAZON"));
		verify(books).save(book);
	}
}
