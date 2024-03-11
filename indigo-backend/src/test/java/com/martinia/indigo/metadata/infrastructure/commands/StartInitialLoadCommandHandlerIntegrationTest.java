package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.metadata.domain.model.commands.StartInitialLoadCommand;
import com.martinia.indigo.metadata.domain.model.events.InitialLoadStartedEvent;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class StartInitialLoadCommandHandlerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private EventBus eventBus;

	@BeforeEach
	void init() {
		insertBook();
		insertAuthor();
		insertTag();
	}

	@Test
	public void startInitialLoadOverrideFalse() {
		// Given
		boolean override = false;

		// When
		commandBus.executeAndWait(StartInitialLoadCommand.builder().override(override).build());

		// Then
		// Verify the method invocation
		verify(eventBus, times(1)).publish(any(InitialLoadStartedEvent.class));
		assertEquals(1, bookRepository.count());
		assertEquals(1, authorRepository.count());
		assertEquals(1, tagRepository.count());
	}

	@Test
	public void startInitialLoadOverrideTrue() {
		// Given
		boolean override = true;

		// When
		commandBus.executeAndWait(StartInitialLoadCommand.builder().override(override).build());

		// Then
		// Verify the method invocation
		verify(eventBus, times(1)).publish(any(InitialLoadStartedEvent.class));
		assertEquals(0, bookRepository.count());
		assertEquals(0, authorRepository.count());
		assertEquals(0, tagRepository.count());
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

	private void insertAuthor() {
		final Map<String, Integer> map = new HashMap<>();
		map.put("es", 1);
		AuthorMongoEntity authorMongoEntity = AuthorMongoEntity.builder()
				.id("id")
				.name("AA. VV.")
				.sort("AA. VV.")
				.numBooks(NumBooksMongo.builder().total(1).languages(map).build())
				.image("::image::")
				.description("::description::")
				.provider("::provider::")
				.lastMetadataSync(new Date())
				.build();
		authorRepository.save(authorMongoEntity);
	}

	private void insertTag() {
		final Map<String, Integer> languages = new HashMap<>();
		languages.put("es", 1);
		final TagMongoEntity tagEntity = TagMongoEntity.builder()
				.id("id")
				.image("image")
				.name("name")
				.numBooks(NumBooksMongo.builder().languages(languages).total(1).build())
				.build();
		tagRepository.save(tagEntity);
	}
}
