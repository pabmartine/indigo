package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillAuthorsMetadataUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class StartFillAuthorsMetadataCommandHandlerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private CommandBus commandBus;

	@Resource
	private StartFillAuthorsMetadataCommandHandler startFillAuthorsMetadataCommandHandler;

	@BeforeEach
	void init() {
		insertBook();
	}

	@Test
	public void startFillAuthorsMetadataNoAuthors() {
		// Given
		boolean override = true;
		String lang = "es";

		// When
		startFillAuthorsMetadataCommandHandler.handle(StartFillAuthorsMetadataCommand.builder().override(override).lang(lang).build());

		// Then
		// Verify the method invocation
		verify(commandBus, times(0)).executeAndWait(any(FindAuthorMetadataCommand.class));
		assertEquals(0, metadataSingleton.getTotal());
		assertEquals(0, metadataSingleton.getCurrent());
	}

	@Test
	public void startFillAuthorsMetadataRunning() {
		// Given
		boolean override = true;
		String lang = "es";
		insertAuthor();
		metadataSingleton.setRunning(false);

		// When
		startFillAuthorsMetadataCommandHandler.handle(StartFillAuthorsMetadataCommand.builder().override(override).lang(lang).build());

		// Then
		// Verify the method invocation
		verify(commandBus, times(0)).executeAndWait(any(FindAuthorMetadataCommand.class));
		assertEquals(1, metadataSingleton.getTotal());
		assertEquals(0, metadataSingleton.getCurrent());
	}

	@Test
	public void startFillAuthorsMetadataOK() {
		// Given
		boolean override = true;
		String lang = "es";
		insertAuthor();
		metadataSingleton.setRunning(true);

		// When
		startFillAuthorsMetadataCommandHandler.handle(StartFillAuthorsMetadataCommand.builder().override(override).lang(lang).build());

		// Then
		// Verify the method invocation
		verify(commandBus, times(1)).executeAndWait(any(FindAuthorMetadataCommand.class));
		assertEquals(0, metadataSingleton.getTotal());
		assertEquals(0, metadataSingleton.getCurrent());
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

	private void insertAuthor() {
		final Map<String, Integer> map = new HashMap<>();
		map.put("es", 1);
		AuthorMongoEntity authorMongoEntity = AuthorMongoEntity.builder()
				.id("id")
				.name("AA. VV.")
				.sort("AA. VV.")
				.numBooks(NumBooksMongo.builder().total(1).languages(map).build())
				.image("::image::")
				.build();
		authorRepository.save(authorMongoEntity);
	}
}
