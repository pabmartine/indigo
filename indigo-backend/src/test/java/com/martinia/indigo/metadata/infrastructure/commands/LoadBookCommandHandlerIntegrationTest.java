package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.adapters.out.sqlite.entities.BookSqliteEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.metadata.domain.model.commands.LoadBookCommand;
import com.martinia.indigo.metadata.domain.model.events.BookLoadedEvent;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
public class LoadBookCommandHandlerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private EventBus eventBus;

	@BeforeEach
	void init() {
		insertBook();
	}

	@Test
	public void loadBookNotOverrideAndPathExist() throws Exception {
		// Given
		String bookId = "123";
		boolean override = false;

		when(bookSqliteRepository.findById(any())).thenReturn(Optional.ofNullable(BookSqliteEntity.builder()
				.path("path").authorSort("VV., AA.")
				.seriesIndex(
				BigDecimal.ONE)
				.build()));
		Mockito.when(serieSqliteRepository.getSerieByBook(BigDecimal.ONE.intValue())).thenReturn(Optional.of("serie"));

		// When
		commandBus.executeAndWait(LoadBookCommand.builder().bookId(bookId).override(override).build());

		// Then
		Mockito.verify(eventBus, times(0)).publish(any(BookLoadedEvent.class));
	}

	@Test
	public void loadBookNotOverrideTagNotExist() throws Exception {
		// Given
		String bookId = "123";
		boolean override = true;

		when(bookSqliteRepository.findById(any())).thenReturn(Optional.ofNullable(BookSqliteEntity.builder()
				.path("path").authorSort("VV., AA.")
				.seriesIndex(
						BigDecimal.ONE)
				.build()));
		Mockito.when(serieSqliteRepository.getSerieByBook(BigDecimal.ONE.intValue())).thenReturn(Optional.of("serie"));
		Mockito.when(tagSqliteRepository.getTagsByBookId(anyInt())).thenReturn(List.of("tag"));
		// When
		commandBus.executeAndWait(LoadBookCommand.builder().bookId(bookId).override(override).build());

		// Then
		assertEquals(1, tagRepository.findByName("tag").size());
		assertEquals(2, bookRepository.count());
		Mockito.verify(eventBus, times(1)).publish(any(BookLoadedEvent.class));
	}

	@Test
	public void loadBookNotOverrideTagExist() throws Exception {
		// Given
		String bookId = "123";
		boolean override = true;

		when(bookSqliteRepository.findById(any())).thenReturn(Optional.ofNullable(BookSqliteEntity.builder()
				.path("path").authorSort("VV., AA.")
				.seriesIndex(
						BigDecimal.ONE)
				.build()));
		Mockito.when(serieSqliteRepository.getSerieByBook(BigDecimal.ONE.intValue())).thenReturn(Optional.of("serie"));
		Mockito.when(tagSqliteRepository.getTagsByBookId(anyInt())).thenReturn(List.of("tag"));
		Mockito.when(languageSqliteRepository.getLanguageByBookId(anyInt())).thenReturn(List.of("es"));

		final Map<String, Integer> map = new HashMap<>();
		map.put("es", 1);
		tagRepository.save(TagMongoEntity.builder().name("tag").numBooks(NumBooksMongo.builder().total(1).languages(map).build()).build());

		// When
		commandBus.executeAndWait(LoadBookCommand.builder().bookId(bookId).override(override).build());

		// Then
		List<TagMongoEntity> tagMongoEntityList = tagRepository.findByName("tag");
		assertEquals(1, tagMongoEntityList.size());
		assertEquals(2, tagMongoEntityList.get(0).getNumBooks().getTotal());
		assertEquals(2, tagMongoEntityList.get(0).getNumBooks().getLanguages().get("es").intValue());
		assertNull(tagMongoEntityList.get(0).getNumBooks().getLanguages().get("en"));

		assertEquals(2, bookRepository.count());
		Mockito.verify(eventBus, times(1)).publish(any(BookLoadedEvent.class));
	}

	@Test
	public void loadBookNotOverrideTagExistEnglish() throws Exception {
		// Given
		String bookId = "123";
		boolean override = true;

		when(bookSqliteRepository.findById(any())).thenReturn(Optional.ofNullable(BookSqliteEntity.builder()
				.path("path").authorSort("VV., AA.")
				.seriesIndex(
						BigDecimal.ONE)
				.build()));
		Mockito.when(serieSqliteRepository.getSerieByBook(BigDecimal.ONE.intValue())).thenReturn(Optional.of("serie"));
		Mockito.when(tagSqliteRepository.getTagsByBookId(anyInt())).thenReturn(List.of("tag"));
		Mockito.when(languageSqliteRepository.getLanguageByBookId(anyInt())).thenReturn(List.of("en"));
		final Map<String, Integer> map = new HashMap<>();
		map.put("es", 1);
		tagRepository.save(TagMongoEntity.builder().name("tag").numBooks(NumBooksMongo.builder().total(1).languages(map).build()).build());

		// When
		commandBus.executeAndWait(LoadBookCommand.builder().bookId(bookId).override(override).build());

		// Then
		List<TagMongoEntity> tagMongoEntityList = tagRepository.findByName("tag");

		assertEquals(1, tagMongoEntityList.size());
		assertEquals(2, tagMongoEntityList.get(0).getNumBooks().getTotal());
		assertEquals(1, tagMongoEntityList.get(0).getNumBooks().getLanguages().get("es").intValue());
		assertEquals(1, tagMongoEntityList.get(0).getNumBooks().getLanguages().get("en").intValue());
		assertEquals(2, bookRepository.count());
		Mockito.verify(eventBus, times(1)).publish(any(BookLoadedEvent.class));
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
