package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class FindBookMetadataCommandHandlerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private EventBus eventBus;

	@BeforeEach
	void init() {
		insertBook();
		doNothing().when(eventBus).publish(any(BookMetadataFoundEvent.class));
	}

	@Test
	public void findBookMetadataNotFound() {
		// Given
		String bookId = "book-123";
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.lastExecution(lastExecution)
				.build();

		// When
		commandBus.executeAndWait(command);

		// Then
		Optional<BookMongoEntity> optEntity = bookRepository.findById("id");
		assertTrue(optEntity.isPresent());
		assertEquals(0f, optEntity.get().getRating(), 0f);
	}

	@Test
	public void findBookMetadataSkipsFreshDynamicMetadata() {
		// Given
		String bookId = "id";
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);
		BookMongoEntity freshBook = bookRepository.findById(bookId).orElseThrow();
		freshBook.setRating(4F);
		freshBook.setRatingAverage(4F);
		freshBook.setRatingUpdatedAt(new Date());
		freshBook.setMetadataMatchStatus("MATCHED");
		bookRepository.save(freshBook);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.lastExecution(lastExecution)
				.build();

		// When
		commandBus.executeAndWait(command);

		// Then
		Optional<BookMongoEntity> optEntity = bookRepository.findById("id");
		assertTrue(optEntity.isPresent());
		assertEquals(4f, optEntity.get().getRating(), 0f);
		verify(findGoogleBooksBookPort, never()).findBook(any(BookMetadataQuery.class));
		verify(findOpenLibraryBookPort, never()).findBook(any(BookMetadataQuery.class));
		verify(eventBus, never()).publish(any(BookMetadataFoundEvent.class));
	}

	@Test
	public void findBookMetadataOverrideOpenLibrary() {
		// Given
		String bookId = "id";
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.lastExecution(lastExecution)
				.build();

		final BookMetadataResult bookData = BookMetadataResult.builder()
				.ratingAverage(5F)
				.ratingsCount(10L)
				.provider(ProviderEnum.OPEN_LIBRARY.name())
				.openLibraryWorkId("OL1W")
				.matchConfidence(1D)
				.build();

		Mockito.doReturn(null).when(findGoogleBooksBookPort).findBook(any(BookMetadataQuery.class));
		Mockito.doReturn(bookData).when(findOpenLibraryBookPort).findBook(any(BookMetadataQuery.class));

		// When
		commandBus.executeAndWait(command);

		// Then
		Optional<BookMongoEntity> optEntity = bookRepository.findById("id");
		assertTrue(optEntity.isPresent());
		assertEquals(5f, optEntity.get().getRating(), 0f);
		assertEquals(5F, optEntity.get().getRatingAverage().floatValue(), 0F);
		assertEquals(10L, optEntity.get().getRatingsCount().longValue());
		assertEquals("OL1W", optEntity.get().getOpenLibraryWorkId());

		verify(eventBus, times(1)).publish(any(BookMetadataFoundEvent.class));
	}

	@Test
	public void findBookMetadataOverrideGoogle() {
		// Given
		String bookId = "id";
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.lastExecution(lastExecution)
				.build();

		BookMongoEntity existingBook = bookRepository.findById(bookId).orElseThrow();
		existingBook.setOpenLibraryWorkId("OL_EXISTING_W");
		existingBook.setOpenLibraryEditionId("OL_EXISTING_M");
		bookRepository.save(existingBook);

		final BookMetadataResult bookData = BookMetadataResult.builder()
				.ratingAverage(5F)
				.provider(ProviderEnum.GOOGLE.name())
				.matchConfidence(1D)
				.build();

		Mockito.doReturn(bookData).when(findGoogleBooksBookPort).findBook(any(BookMetadataQuery.class));

		// When
		commandBus.executeAndWait(command);

		// Then
		Optional<BookMongoEntity> optEntity = bookRepository.findById("id");
		assertTrue(optEntity.isPresent());
		assertEquals(5f, optEntity.get().getRating(), 0f);
		assertEquals("OL_EXISTING_W", optEntity.get().getOpenLibraryWorkId());
		assertEquals("OL_EXISTING_M", optEntity.get().getOpenLibraryEditionId());

		verify(eventBus, times(1)).publish(any(BookMetadataFoundEvent.class));
	}

	@Test
	void exactOpenLibraryIdentityIsCombinedWithGoogleRating() {
		BookMongoEntity book = bookRepository.findById("id").orElseThrow();
		book.setIsbn13(List.of("9780261102217"));
		bookRepository.save(book);
		Mockito.doReturn(BookMetadataResult.builder()
				.provider(ProviderEnum.OPEN_LIBRARY.name())
				.openLibraryWorkId("OL1W")
				.openLibraryEditionId("OL1M")
				.matchConfidence(1D)
				.build()).when(findOpenLibraryBookPort).findBook(any(BookMetadataQuery.class));
		Mockito.doReturn(BookMetadataResult.builder()
				.ratingAverage(4.5F)
				.ratingsCount(25L)
				.provider(ProviderEnum.GOOGLE.name())
				.matchConfidence(0.9D)
				.build()).when(findGoogleBooksBookPort).findBook(any(BookMetadataQuery.class));

		commandBus.executeAndWait(bookMetadataCommand("id"));

		BookMongoEntity updatedBook = bookRepository.findById("id").orElseThrow();
		assertEquals("OL1W", updatedBook.getOpenLibraryWorkId());
		assertEquals("OL1M", updatedBook.getOpenLibraryEditionId());
		assertEquals(4.5F, updatedBook.getRatingAverage(), 0F);
		assertEquals(ProviderEnum.GOOGLE.name(), updatedBook.getRatingProvider());
		assertEquals(0.9D, updatedBook.getMetadataMatchConfidence(), 0D);
	}

	private void insertBook() {
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
				.build();
		bookRepository.save(bookMongoEntity);
	}

	private FindBookMetadataCommand bookMetadataCommand(final String bookId) {
		return FindBookMetadataCommand.builder()
				.bookId(bookId)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.build();
	}
}
