package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.adapters.out.sqlite.entities.AuthorSqliteEntity;
import com.martinia.indigo.adapters.out.sqlite.entities.BookSqliteEntity;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.metadata.domain.model.events.BookLoadedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FillAuthorsBookLoadedEventListenerIntegrationTest extends BaseIndigoIntegrationTest {

	@Test
	public void fillAuthorsBooksEmptyAuthors() {
		// Given
		final BookSqliteEntity bookSqlEntity = BookSqliteEntity.builder()
				.id(1)
				.path("path")
				.title("title")
				.pubDate("2000-01-01 00:00:00+00:00")
				.lastModified("2000-01-01 00:00:00+00:00")
				.seriesIndex(BigDecimal.ONE)
				.authorSort("")
				.build();
		Mockito.when(bookSqliteRepository.findById(bookSqlEntity.getId())).thenReturn(Optional.of(bookSqlEntity));

		BookLoadedEvent event = BookLoadedEvent.builder().bookId(bookSqlEntity.getId()+"").build();

		// When
		eventBus.publish(event);

		// Then
		assertTrue(bookRepository.findByTitle(bookSqlEntity.getTitle()).isEmpty());
	}

	@Test
	public void fillAuthorsBooksAuthorsNotFound() {
		// Given
		final BookSqliteEntity bookSqlEntity = BookSqliteEntity.builder()
				.id(1)
				.path("path")
				.title("title")
				.pubDate("2000-01-01 00:00:00+00:00")
				.lastModified("2000-01-01 00:00:00+00:00")
				.seriesIndex(BigDecimal.ONE)
				.authorSort("author")
				.build();
		Mockito.when(bookSqliteRepository.findById(bookSqlEntity.getId())).thenReturn(Optional.of(bookSqlEntity));

		BookLoadedEvent event = BookLoadedEvent.builder().bookId(bookSqlEntity.getId()+"").build();

		// When
		eventBus.publish(event);

		// Then
		assertTrue(bookRepository.findByTitle(bookSqlEntity.getTitle()).isEmpty());
	}

	@Test
	public void fillAuthorsBooksNotExist() {
		// Given
		final BookSqliteEntity bookSqlEntity = BookSqliteEntity.builder()
				.id(1)
				.path("path")
				.title("title")
				.pubDate("2000-01-01 00:00:00+00:00")
				.lastModified("2000-01-01 00:00:00+00:00")
				.seriesIndex(BigDecimal.ONE)
				.authorSort("AA. VV.")
				.build();

		final AuthorSqliteEntity authorSqlEntity = AuthorSqliteEntity.builder()
				.id(1)
				.name("VV., AA.")
				.sort("VV., AA.")
				.build();

		Mockito.when(bookSqliteRepository.findById(bookSqlEntity.getId())).thenReturn(Optional.of(bookSqlEntity));
		Mockito.when(authorSqliteRepository.findAuthors(bookSqlEntity.getId())).thenReturn(Arrays.asList(authorSqlEntity));

		BookLoadedEvent event = BookLoadedEvent.builder().bookId(bookSqlEntity.getId()+"").build();

		// When
		eventBus.publish(event);

		// Then
		assertTrue(bookRepository.findByTitle(bookSqlEntity.getTitle()).isEmpty());
		assertEquals(1, authorRepository.findByName("AA. VV.").size());
	}

	@Test
	public void fillAuthorsBooksOk() {
		// Given

		insertBook();

		final BookSqliteEntity bookSqlEntity = BookSqliteEntity.builder()
				.id(1)
				.path("path")
				.title("title")
				.pubDate("2000-01-01 00:00:00+00:00")
				.lastModified("2000-01-01 00:00:00+00:00")
				.seriesIndex(BigDecimal.ONE)
				.authorSort("author")
				.build();

		final AuthorSqliteEntity authorSqlEntity = AuthorSqliteEntity.builder()
				.id(1)
				.name("VV., AA.")
				.sort("VV., AA.")
				.build();

		Mockito.when(bookSqliteRepository.findById(bookSqlEntity.getId())).thenReturn(Optional.of(bookSqlEntity));
		Mockito.when(authorSqliteRepository.findAuthors(bookSqlEntity.getId())).thenReturn(Arrays.asList(authorSqlEntity));

		BookLoadedEvent event = BookLoadedEvent.builder().bookId(bookSqlEntity.getId()+"").build();

		// When
		eventBus.publish(event);

		// Then
		assertTrue(bookRepository.findByTitle(bookSqlEntity.getTitle()).isPresent());
		assertTrue(bookRepository.findByTitle(bookSqlEntity.getTitle()).get().getAuthors().contains("AA. VV."));
		assertEquals(1, authorRepository.findByName("AA. VV.").size());
	}

	@Test
	public void fillAuthorsBooksWithAuthorsOk() {
		// Given

		insertBook();
		insertAuthor();

		final BookSqliteEntity bookSqlEntity = BookSqliteEntity.builder()
				.id(1)
				.path("path")
				.title("title")
				.pubDate("2000-01-01 00:00:00+00:00")
				.lastModified("2000-01-01 00:00:00+00:00")
				.seriesIndex(BigDecimal.ONE)
				.authorSort("author")
				.build();

		final AuthorSqliteEntity authorSqlEntity = AuthorSqliteEntity.builder()
				.id(1)
				.name("VV., AA.")
				.sort("VV., AA.")
				.build();

		Mockito.when(bookSqliteRepository.findById(bookSqlEntity.getId())).thenReturn(Optional.of(bookSqlEntity));
		Mockito.when(authorSqliteRepository.findAuthors(bookSqlEntity.getId())).thenReturn(Arrays.asList(authorSqlEntity));

		BookLoadedEvent event = BookLoadedEvent.builder().bookId(bookSqlEntity.getId()+"").build();

		// When
		eventBus.publish(event);

		// Then
		assertTrue(bookRepository.findByTitle(bookSqlEntity.getTitle()).isPresent());
		assertTrue(bookRepository.findByTitle(bookSqlEntity.getTitle()).get().getAuthors().contains("AA. VV."));
		assertEquals(1, authorRepository.findByName("AA. VV.").size());
		assertEquals(2, authorRepository.findByName("AA. VV.").get(0).getNumBooks().getTotal());
	}

	private void insertBook() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("id")
				.title("title")
				.path("path")
				.languages(List.of("es"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("author", "AA. VV." ))
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
				.build();
		authorRepository.save(authorMongoEntity);
	}
}
