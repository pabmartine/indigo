package com.martinia.indigo.metadata.application.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class AuthorPreparationTest {
	private final BookRepository books = mock(BookRepository.class);
	private final AuthorRepository authors = mock(AuthorRepository.class);
	private final CommandBus commands = mock(CommandBus.class);
	private final MetadataSingleton status = new MetadataSingleton();
	private final StartFillAuthorsMetadataUseCaseImpl useCase = new StartFillAuthorsMetadataUseCaseImpl();
	private long runId;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(useCase, "bookRepository", books);
		ReflectionTestUtils.setField(useCase, "authorRepository", authors);
		ReflectionTestUtils.setField(useCase, "commandBus", commands);
		ReflectionTestUtils.setField(useCase, "metadataSingleton", status);
		runId = status.start("PARTIAL", "AUTHORS");
		when(books.getBookLanguages()).thenReturn(List.of("es", "en"));
		when(authors.count(anyList())).thenReturn(0L);
	}

	@Test
	void readsProjectedBatchesAndRegistersOnlyMissingAuthorsWithBookStatistics() {
		when(books.findAuthorNamesBatch(null)).thenReturn(List.of(
				BookMongoEntity.builder().id("01").authors(List.of("Alice", "Bob")).languages(List.of("es")).build(),
				BookMongoEntity.builder().id("02").authors(List.of("Alice", "VV., AA.")).languages(List.of("en")).build()));
		when(books.findAuthorNamesBatch("02")).thenReturn(List.of());
		when(authors.findNamesByNameIn(anyList())).thenReturn(List.of(AuthorMongoEntity.builder().name("Bob").build()));

		useCase.start(false, "es", runId);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Iterable<AuthorMongoEntity>> saved = ArgumentCaptor.forClass(Iterable.class);
		verify(authors).saveAll(saved.capture());
		assertThat(saved.getValue()).extracting(AuthorMongoEntity::getName).containsExactly("Alice", "AA. VV.");
		assertThat(saved.getValue()).filteredOn(author -> author.getName().equals("Alice")).singleElement().satisfies(author -> {
			assertThat(author.getNumBooks().getTotal()).isEqualTo(2);
			assertThat(author.getNumBooks().getLanguages()).containsEntry("es", 1).containsEntry("en", 1);
		});
		verify(authors, never()).findByName(anyString());
		verify(books, never()).count();
		verify(books, never()).findAll(isNull(), anyInt(), anyInt(), anyString(), anyString());
		assertThat(status.isActive(runId)).isFalse();
	}

	@Test
	void logsPeriodicallyWhileAQueryWaitsAndStopsLoggingAfterCancellation(final CapturedOutput output) throws Exception {
		ReflectionTestUtils.setField(useCase, "progressLogIntervalMillis", 50L);
		CountDownLatch enteredQuery = new CountDownLatch(1);
		CountDownLatch releaseQuery = new CountDownLatch(1);
		when(books.findAuthorNamesBatch(null)).thenAnswer(invocation -> {
			enteredQuery.countDown();
			assertThat(releaseQuery.await(5, TimeUnit.SECONDS)).isTrue();
			return List.of(BookMongoEntity.builder().id("01").authors(List.of("Alice")).build());
		});
		CompletableFuture<Void> task = CompletableFuture.runAsync(() -> useCase.start(false, "es", runId));
		try {
			assertThat(enteredQuery.await(2, TimeUnit.SECONDS)).isTrue();
			Thread.sleep(200L);
			assertThat(logCount(output)).isGreaterThanOrEqualTo(3);
			assertThat(output.getOut()).contains("stage=Leyendo autores e idiomas de los libros", "booksRead=0", "stageElapsedMs=");
			status.stop("AUTHORS");
		}
		finally {
			releaseQuery.countDown();
			task.get(5, TimeUnit.SECONDS);
		}
		long count = logCount(output);
		Thread.sleep(150L);
		assertThat(logCount(output)).isEqualTo(count);
		verify(authors, never()).saveAll(any());
		verify(books, never()).getBookLanguages();
	}

	@Test
	void finishesTheRunAndLogsPreparationFailures(final CapturedOutput output) {
		when(books.findAuthorNamesBatch(null)).thenThrow(new IllegalStateException("Mongo unavailable"));

		assertThatThrownBy(() -> useCase.start(false, "es", runId)).hasMessage("Mongo unavailable");
		assertThat(status.isActive(runId)).isFalse();
		assertThat(output.getOut()).contains("Author metadata run " + runId + " failed", "Mongo unavailable");
	}

	private long logCount(CapturedOutput output) {
		return output.getOut().lines().filter(line -> line.contains("Author metadata run " + runId + " progress:")).count();
	}
}
