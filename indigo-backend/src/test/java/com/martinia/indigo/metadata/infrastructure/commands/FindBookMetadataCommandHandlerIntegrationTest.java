package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsBookPort;
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
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
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
	public void findBookMetadataNotOverride() {
		// Given
		String bookId = "id";
		boolean override = false;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
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
	public void findBookMetadataOverrideGoodReads() {
		// Given
		String bookId = "id";
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
				.lastExecution(lastExecution)
				.build();

		final String[] bookData = new String[3];
		bookData[0] = "5";
		bookData[1] = "similar";
		bookData[2] = ProviderEnum.GOODREADS.name();

		Mockito.doReturn(bookData).when(findGoodReadsBookPort).findBook(anyString(), anyString(), anyList(), anyBoolean());
		Mockito.doReturn(null).when(findGoogleBooksBookPort).findBook(anyString(), anyList());

		// When
		commandBus.executeAndWait(command);

		// Then
		Optional<BookMongoEntity> optEntity = bookRepository.findById("id");
		assertTrue(optEntity.isPresent());
		assertEquals(0f, optEntity.get().getRating(), 0f);

		verify(eventBus, times(1)).publish(any(BookMetadataFoundEvent.class));
	}

	@Test
	public void findBookMetadataOverrideGoogle() {
		// Given
		String bookId = "id";
		boolean override = true;
		long lastExecution = LocalDateTime.now().getLong(ChronoField.CLOCK_HOUR_OF_DAY);

		FindBookMetadataCommand command = FindBookMetadataCommand.builder()
				.bookId(bookId)
				.override(override)
				.lastExecution(lastExecution)
				.build();

		final String[] bookData = new String[3];
		bookData[0] = "5";
		bookData[1] = "similar";
		bookData[2] = ProviderEnum.GOOGLE.name();

		Mockito.doReturn(null).when(findGoodReadsBookPort).findBook(anyString(), anyString(), anyList(), anyBoolean());
		Mockito.doReturn(bookData).when(findGoogleBooksBookPort).findBook(anyString(), anyList());

		// When
		commandBus.executeAndWait(command);

		// Then
		Optional<BookMongoEntity> optEntity = bookRepository.findById("id");
		assertTrue(optEntity.isPresent());
		assertEquals(5f, optEntity.get().getRating(), 0f);

		verify(eventBus, times(1)).publish(any(BookMetadataFoundEvent.class));
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
}
