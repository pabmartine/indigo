package com.martinia.indigo.metadata.infrastructure.events;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.adapters.out.sqlite.entities.BookSqliteEntity;
import com.martinia.indigo.adapters.out.sqlite.repository.BookSqliteRepository;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.commands.LoadBookCommand;
import com.martinia.indigo.metadata.domain.model.events.InitialLoadStartedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@SpringBootTest
public class InitialLoadStartedEventListenerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private CommandBus commandBus;

	@BeforeEach
	void init(){
		metadataSingleton.stop();
	}

	@Test
	public void initialLoadStartedWithoutBooks() {
		// Given

		Mockito.when(bookSqliteRepository.count()).thenReturn(0L);

		boolean override = true;
		InitialLoadStartedEvent event = InitialLoadStartedEvent.builder().override(override).build();

		// When
		eventBus.publish(event);

		// Then
		assertEquals(0L, metadataSingleton.getTotal());
	}

	@Test
	public void initialLoadStartedMetadataRunning() {
		// Given

		metadataSingleton.setRunning(false);

		Mockito.when(bookSqliteRepository.count()).thenReturn(1L);
		final BookSqliteEntity bookSqlEntity = BookSqliteEntity.builder()
				.id(1)
				.path("path")
				.title("title")
				.pubDate("2000-01-01 00:00:00+00:00")
				.lastModified("2000-01-01 00:00:00+00:00")
				.seriesIndex(BigDecimal.ONE)
				.authorSort("author")
				.build();
		final Page<BookSqliteEntity> page = Mockito.mock(Page.class);
		Mockito.when(page.getContent()).thenReturn(Arrays.asList(bookSqlEntity));
		Mockito.when(bookSqliteRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

		boolean override = true;
		InitialLoadStartedEvent event = InitialLoadStartedEvent.builder().override(override).build();

		// When
		eventBus.publish(event);

		// Then
		assertEquals(1L, metadataSingleton.getTotal());
		assertEquals(0L, metadataSingleton.getCurrent());
		Mockito.verify(commandBus, times(0)).execute(Mockito.any(LoadBookCommand.class));
	}

	@Test
	public void initialLoadStartedOK() {
		// Given
		metadataSingleton.setRunning(true);

		Mockito.when(bookSqliteRepository.count()).thenReturn(1L);
		final BookSqliteEntity bookSqlEntity = BookSqliteEntity.builder()
				.id(1)
				.path("path")
				.title("title")
				.pubDate("2000-01-01 00:00:00+00:00")
				.lastModified("2000-01-01 00:00:00+00:00")
				.seriesIndex(BigDecimal.ONE)
				.authorSort("author")
				.build();
		final Page<BookSqliteEntity> page = Mockito.mock(Page.class);
		Mockito.when(page.getContent()).thenReturn(Arrays.asList(bookSqlEntity));
		Mockito.when(bookSqliteRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

		boolean override = true;
		InitialLoadStartedEvent event = InitialLoadStartedEvent.builder().override(override).build();

		// When
		eventBus.publish(event);

		// Then
		assertEquals(0L, metadataSingleton.getTotal());
		assertEquals(0L, metadataSingleton.getCurrent());
		Mockito.verify(commandBus, times(1)).execute(Mockito.any(LoadBookCommand.class));
	}
}
