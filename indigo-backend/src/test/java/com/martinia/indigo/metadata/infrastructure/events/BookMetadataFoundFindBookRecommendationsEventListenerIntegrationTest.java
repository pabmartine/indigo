package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
public class BookMetadataFoundFindBookRecommendationsEventListenerIntegrationTest extends BaseIndigoIntegrationTest {

	@BeforeEach
	public void init(){
		doNothing().when(bookMetadataFoundFindSimilarBooksEventListener).handle(any(BookMetadataFoundEvent.class));
	}

	@Test
	public void findBookRecommendationsBookNotFound() {
		// Given
		final BookMetadataFoundEvent event = BookMetadataFoundEvent.builder().bookId("unknown").build();
		// When
		eventBus.publish(event);

		// Then
		assertTrue(bookRepository.findById(event.getBookId()).isEmpty());
	}

	@Test
	public void findBookRecommendationsNotFound() {
		// Given
		insertBook();
		final BookMetadataFoundEvent event = BookMetadataFoundEvent.builder().bookId("id").build();

		// When
		eventBus.publish(event);

		// Then
		Optional<BookMongoEntity> entity = bookRepository.findById(event.getBookId());
		assertTrue(entity.isPresent());
		assertNull(entity.get().getRecommendations());
	}

	@Test
	public void findBookRecommendationsOk() {
		// Given
		insertBook();
		insertOtherBook();

		final BookMetadataFoundEvent event = BookMetadataFoundEvent.builder().bookId("id").build();

		// When
		eventBus.publish(event);

		// Then
		Optional<BookMongoEntity> entity = bookRepository.findById(event.getBookId());
		assertTrue(entity.isPresent());
		assertNotNull(entity.get().getRecommendations());
		assertEquals("id2", entity.get().getRecommendations().get(0));
	}

	private void insertBook() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("id")
				.title("title")
				.path("path")
				.languages(List.of("es"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.serie(SerieMongo.builder().index(1).name("Serie1").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image("::image::")
				.build();
		bookRepository.save(bookMongoEntity);
	}

	private void insertOtherBook() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("id2")
				.title("title2")
				.path("path2")
				.languages(List.of("es"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author"))
				.serie(SerieMongo.builder().index(1).name("Serie1").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image("::image::")
				.build();
		bookRepository.save(bookMongoEntity);
	}
}
