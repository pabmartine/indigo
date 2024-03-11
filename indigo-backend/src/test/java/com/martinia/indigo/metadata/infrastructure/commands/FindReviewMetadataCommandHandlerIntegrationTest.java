package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.metadata.domain.model.commands.FindReviewMetadataCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class FindReviewMetadataCommandHandlerIntegrationTest extends BaseIndigoIntegrationTest {

	@BeforeEach
	void init() {
		insertBook();
	}

	@Test
	public void findReviewMetadataBookNotFound() {
		// Given
		String bookId = "book-123";
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);
		String lang = "en";

		FindReviewMetadataCommand command = FindReviewMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
				.lastExecution(lastExecution)
				.lang(lang)
				.build();

		// When
		commandBus.executeAndWait(command);

		// Then
		verify(findGoodReadsReviewsPort, times(0)).getReviews(any(), any(), any());
		verify(findAmazonReviewsPort, times(0)).getReviews(any(), any());
		assertTrue(bookRepository.findById(bookId).isEmpty());
	}

	@Test
	public void findReviewMetadataBookOverrideFalse() {
		// Given
		String bookId = "id";
		boolean override = false;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);
		String lang = "en";

		FindReviewMetadataCommand command = FindReviewMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
				.lastExecution(lastExecution)
				.lang(lang)
				.build();

		// When
		commandBus.executeAndWait(command);

		// Then
		verify(findGoodReadsReviewsPort, times(0)).getReviews(any(), any(), any());
		verify(findAmazonReviewsPort, times(0)).getReviews(any(), any());
		assertNull(bookRepository.findById(bookId).get().getReviews().get(0).getName());
	}

	@Test
	public void findReviewMetadataNull() {
		// Given
		String bookId = "id";
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);
		String lang = "en";

		FindReviewMetadataCommand command = FindReviewMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
				.lastExecution(lastExecution)
				.lang(lang)
				.build();

		doReturn(Collections.emptyList()).when(findGoodReadsReviewsPort).getReviews(any(), any(), any());
		doReturn(Collections.emptyList()).when(findAmazonReviewsPort).getReviews(any(), any());

		// When
		commandBus.executeAndWait(command);

		// Then
		verify(findGoodReadsReviewsPort, times(1)).getReviews(any(), any(), any());
		verify(findAmazonReviewsPort, times(1)).getReviews(any(), any());
		assertNull(bookRepository.findById(bookId).get().getReviews());
	}

	@Test
	public void findReviewMetadataOk() {
		// Given
		String bookId = "id";
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);
		String lang = "en";

		FindReviewMetadataCommand command = FindReviewMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
				.lastExecution(lastExecution)
				.lang(lang)
				.build();

		final List<ReviewDto> reviews = Arrays.asList(ReviewDto.builder().name("name").comment("comment").date(new Date()).title("title").rating(5).build());
		doReturn(Collections.emptyList()).when(findGoodReadsReviewsPort).getReviews(any(), any(), any());
		doReturn(reviews).when(findAmazonReviewsPort).getReviews(any(), any());

		// When
		commandBus.executeAndWait(command);

		// Then
		verify(findGoodReadsReviewsPort, times(1)).getReviews(any(), any(), any());
		verify(findAmazonReviewsPort, times(1)).getReviews(any(), any());
		ReviewMongo review = bookRepository.findById(bookId).get().getReviews().get(0);
		assertEquals("name", review.getName());
		assertEquals("comment", review.getComment());
		assertEquals("title", review.getTitle());
		assertEquals(5, review.getRating());
	}

	private void insertBook() {
		final ReviewMongo review = ReviewMongo.builder().lastMetadataSync(new Date()).build();
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("id")
				.title("title")
				.path("path")
				.languages(List.of("es"))
				.similar(Collections.emptyList())
				.authors(Arrays.asList("AA. VV."))
				.serie(SerieMongo.builder().index(1).name("Serie1").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.rating(0F)
				.provider(null)
				.image("::image::")
				.lastMetadataSync(new Date())
				.reviews(Arrays.asList(review))
				.build();
		bookRepository.save(bookMongoEntity);
	}
}
