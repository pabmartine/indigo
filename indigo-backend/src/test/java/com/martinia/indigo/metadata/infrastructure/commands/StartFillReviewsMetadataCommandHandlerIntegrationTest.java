package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.FindReviewMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillReviewsMetadataCommand;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class StartFillReviewsMetadataCommandHandlerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private CommandBus commandBus;

	@Resource
	private StartFillReviewsMetadataCommandHandler startFillReviewsMetadataCommandHandler;

	@Test
	public void startFillReviewsMetadataBookNotFound() {
		// Given
		boolean override = true;
		String lang = "en";

		// When
		startFillReviewsMetadataCommandHandler.handle(StartFillReviewsMetadataCommand.builder().override(override).lang(lang).build());

		// Then
		// Verify the method invocation
		verify(commandBus, times(0)).executeAndWait(any(FindReviewMetadataCommand.class));
//		assertEquals(0, metadataSingleton.getTotal());
//		assertEquals(0, metadataSingleton.getCurrent());
	}

	@Test
	public void startFillReviewsMetadataSingletonNotRunning() {
		// Given
		boolean override = true;
		String lang = "en";
		insertBook();
		metadataSingleton.setRunning(false);
		// When
		startFillReviewsMetadataCommandHandler.handle(StartFillReviewsMetadataCommand.builder().override(override).lang(lang).build());

		// Then
		// Verify the method invocation
		verify(commandBus, times(0)).executeAndWait(any(FindReviewMetadataCommand.class));
//		assertEquals(1, metadataSingleton.getTotal());
//		assertEquals(0, metadataSingleton.getCurrent());
	}

	@Test
	public void startFillReviewsMetadataSingletonOK() {
		// Given
		boolean override = true;
		String lang = "en";
		insertBook();
		metadataSingleton.setRunning(true);
		// When
		startFillReviewsMetadataCommandHandler.handle(StartFillReviewsMetadataCommand.builder().override(override).lang(lang).build());

		// Then
		// Verify the method invocation
		verify(commandBus, times(1)).executeAndWait(any(FindReviewMetadataCommand.class));
//		assertEquals(0, metadataSingleton.getTotal());
//		assertEquals(0, metadataSingleton.getCurrent());
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
