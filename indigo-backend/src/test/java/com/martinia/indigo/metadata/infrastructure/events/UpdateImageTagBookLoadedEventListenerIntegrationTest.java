package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.adapters.out.sqlite.repository.BookSqliteRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.model.commands.FindImageTagMetadataCommand;
import com.martinia.indigo.metadata.domain.model.events.BookLoadedEvent;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

class UpdateImageTagBookLoadedEventListenerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private CommandBus commandBus;


	@BeforeEach
	void init(){
		doNothing().when(fillAuthorsBookLoadedEventListener).handle(any(BookLoadedEvent.class));

	}

	@Test
	public void updateImageTagTagsNotPresent() {
		// Given
		BookLoadedEvent event = BookLoadedEvent.builder().bookId("id").build();
		insertBook();
		// When
		eventBus.publish(event);

		// Then

		assertTrue(bookRepository.findById("id").isPresent());
		assertTrue(tagRepository.findById("tag").isEmpty());
	}

	@Test
	public void updateImageTagTagsOk() {
		// Given
		BookLoadedEvent event = BookLoadedEvent.builder().bookId("id").build();
		insertBook();
		insertTag();
		Mockito.when(commandBus.executeAndWait(Mockito.any(FindImageTagMetadataCommand.class))).thenReturn("new_imaga");
		// When
		eventBus.publish(event);

		// Then

		assertTrue(bookRepository.findById("id").isPresent());
		assertTrue(tagRepository.findById("tag").isPresent());
		assertEquals("new_imaga", tagRepository.findById("tag").get().getImage());
		Mockito.verify(commandBus, Mockito.atLeast(1)).executeAndWait(Mockito.any());
	}

	private void insertBook() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("id")
				.title("title")
				.path("path")
				.languages(List.of("es"))
				.authors(Arrays.asList("author"))
				.serie(SerieMongo.builder().index(1).name("Serie1").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image("::image::")
				.build();
		bookRepository.save(bookMongoEntity);
	}

	private void insertTag() {
		TagMongoEntity tagMongoEntity = TagMongoEntity.builder()
				.id("tag")
				.name("tag")
				.image("::image::")
				.build();
		tagRepository.save(tagMongoEntity);
	}
}