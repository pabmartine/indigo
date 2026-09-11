package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.model.BookMetadataScope;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class StartFillBooksMetadataCommandHandlerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private CommandBus commandBus;

	@Resource
	private StartFillBooksMetadataCommandHandler startFillBooksMetadataCommandHandler;

	@BeforeEach
	public void init(){
		metadataSingleton.start("FULL", "BOOKS");
		metadataSingleton.stop();
		bookRepository.deleteAll();
		when(commandBus.executeAndWait(any(FindBookMetadataCommand.class))).thenReturn(MetadataItemResult.FOUND);
	}

	@Test
	public void startFillBooksMetadataNoBooks() {
		// Given
		// When
		startFillBooksMetadataCommandHandler.handle(allBooksCommand());

		// Then
		// Verify the method invocation
		verify(commandBus, times(0)).executeAndWait(any(FindBookMetadataCommand.class));
		assertEquals(0, metadataSingleton.getTotal());
		assertEquals(0, metadataSingleton.getCurrent());
	}

	@Test
	public void startFillBooksMetadataSingletonNotRunning() {
		// Given
		insertBook();
		metadataSingleton.setRunning(false);
		// When
		startFillBooksMetadataCommandHandler.handle(allBooksCommand());

		// Then
		// Verify the method invocation
		verify(commandBus, times(0)).executeAndWait(any(FindBookMetadataCommand.class));
		assertEquals(1, metadataSingleton.getTotal());
		assertEquals(0, metadataSingleton.getCurrent());
	}

	@Test
	public void startFillBooksMetadataSingletonOK() {
		// Given
		insertBook();
		metadataSingleton.setRunning(true);
		// When
		startFillBooksMetadataCommandHandler.handle(allBooksCommand());

		// Then
		// Verify the method invocation
		verify(commandBus, times(1)).executeAndWait(any(FindBookMetadataCommand.class));
		assertEquals(1, metadataSingleton.getTotal());
		assertEquals(1, metadataSingleton.getCurrent());
	}

	@Test
	void incompleteScopeSelectsOnlyBooksMissingIntrinsicMetadata() {
		insertBook();
		bookRepository.save(BookMongoEntity.builder()
				.id("complete")
				.title("Complete book")
				.path("complete-path")
				.comment("Description")
				.pubDate(new Date())
				.languages(List.of("es"))
				.authors(List.of("Author"))
				.pages(100)
				.tags(List.of("tag"))
				.image("::image::")
				.isbn13(List.of("9780261102217"))
				.identifiers(Map.of("ISBN", List.of("9780261102217")))
				.build());
		metadataSingleton.setRunning(true);

		startFillBooksMetadataCommandHandler.handle(incompleteBooksCommand());

		verify(commandBus, times(1)).executeAndWait(any(FindBookMetadataCommand.class));
		assertEquals(1, metadataSingleton.getTotal());
	}

	@Test
	void incompleteScopeDoesNotRetryAnUnchangedNoMatch() {
		insertBook();
		BookMongoEntity book = bookRepository.findById("id").orElseThrow();
		book.setMetadataMatchStatus("NO_MATCH");
		bookRepository.save(book);
		metadataSingleton.setRunning(true);

		startFillBooksMetadataCommandHandler.handle(incompleteBooksCommand());

		verify(commandBus, times(0)).executeAndWait(any(FindBookMetadataCommand.class));
		assertEquals(0, metadataSingleton.getTotal());
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

	private StartFillBooksMetadataCommand allBooksCommand() {
		return StartFillBooksMetadataCommand.builder()
				.scope(BookMetadataScope.ALL)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.build();
	}

	private StartFillBooksMetadataCommand incompleteBooksCommand() {
		return StartFillBooksMetadataCommand.builder()
				.scope(BookMetadataScope.INCOMPLETE)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.build();
	}
}
