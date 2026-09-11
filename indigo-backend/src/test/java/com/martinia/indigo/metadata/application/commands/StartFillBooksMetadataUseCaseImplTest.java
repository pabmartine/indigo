package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.BookMetadataScope;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class StartFillBooksMetadataUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private MetadataSingleton metadataSingleton;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private CommandBus commandBus;

	@Resource
	private StartFillBooksMetadataUseCaseImpl startFillBooksMetadataUseCase;

	@Test
	public void testStart_All_ShouldFindMetadataForEachBook() {
		// Given
		List<BookMongoEntity> books = List.of(BookMongoEntity.builder().id("one").build(),
				BookMongoEntity.builder().id("two").build());

		when(bookRepository.findAllBookIds()).thenReturn(books);
		doNothing().when(metadataSingleton).setMessage(anyString());
		doNothing().when(metadataSingleton).setTotal(anyLong());
		when(metadataSingleton.isRunning()).thenReturn(true);

		// When
		startFillBooksMetadataUseCase.start(BookMetadataScope.ALL, MetadataMergePolicy.FILL_MISSING,
				DynamicMetadataPolicy.REFRESH_IF_STALE);

		// Then
		verify(bookRepository).findAllBookIds();
		verify(bookRepository, never()).findBooksWithIncompleteMetadata();
		verify(commandBus, times(books.size())).executeAndWait(any(FindBookMetadataCommand.class));
		verify(metadataSingleton, times(1)).setMessage(anyString());
		verify(metadataSingleton, times(1)).setTotal(anyLong());
	}

	@Test
	public void testStart_MetadataSingletonNotRunning_ShouldStopProcessing() {
		// Given
		List<BookMongoEntity> books = List.of(BookMongoEntity.builder().id("one").build());

		when(bookRepository.findAllBookIds()).thenReturn(books);
		when(metadataSingleton.isRunning()).thenReturn(false);

		// When
		startFillBooksMetadataUseCase.start(BookMetadataScope.ALL, MetadataMergePolicy.FILL_MISSING,
				DynamicMetadataPolicy.REFRESH_IF_STALE);

		// Then
		verify(commandBus, never()).executeAndWait(any(FindBookMetadataCommand.class));
	}

	@Test
	void shouldSelectIncompleteBooksInMongo() {
		when(bookRepository.findBooksWithIncompleteMetadata())
				.thenReturn(List.of(BookMongoEntity.builder().id("incomplete").build()));
		when(metadataSingleton.isRunning()).thenReturn(true);

		startFillBooksMetadataUseCase.start(BookMetadataScope.INCOMPLETE, MetadataMergePolicy.FILL_MISSING,
				DynamicMetadataPolicy.REFRESH_IF_STALE);

		verify(bookRepository).findBooksWithIncompleteMetadata();
		verify(bookRepository, never()).findAllBookIds();
		ArgumentCaptor<FindBookMetadataCommand> commandCaptor = ArgumentCaptor.forClass(FindBookMetadataCommand.class);
		verify(commandBus).executeAndWait(commandCaptor.capture());
		FindBookMetadataCommand command = commandCaptor.getValue();
		assertEquals("incomplete", command.getBookId());
		assertEquals(MetadataMergePolicy.FILL_MISSING, command.getMergePolicy());
		assertEquals(DynamicMetadataPolicy.REFRESH_IF_STALE, command.getDynamicPolicy());
	}

}
