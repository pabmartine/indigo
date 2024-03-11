package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
	public void testStart_OverrideTrue_ShouldFindMetadataForEachBook() {
		// Given
		boolean override = true;

		List<BookMongoEntity> books = new ArrayList<>();
		// Agregar libros a la lista 'books' para simular resultados del repositorio
		// ...

		when(bookRepository.count()).thenReturn((long) books.size());
		when(bookRepository.findAll(null, 0, 100, "id", "asc")).thenReturn(books);
		doNothing().when(metadataSingleton).setMessage(anyString());
		doNothing().when(metadataSingleton).setTotal(anyLong());
		doNothing().when(metadataSingleton).setCurrent(anyLong());
		doNothing().when(metadataSingleton).increase();
		when(metadataSingleton.isRunning()).thenReturn(true);
		// Configurar el comportamiento esperado del commandBus.executeAndWait()
		// ...

		// When
		startFillBooksMetadataUseCase.start(override);

		// Then
		// Verificar que se llama al método count() del repositorio
		verify(bookRepository, times(1)).count();
		// Verificar que se llama al método executeAndWait() del commandBus por cada libro
		verify(commandBus, times(books.size())).executeAndWait(any(FindBookMetadataCommand.class));
		// Verificar que se llama a los métodos de metadataSingleton para actualizar el estado
		verify(metadataSingleton, times(1)).setMessage(anyString());
		verify(metadataSingleton, times(1)).setTotal(anyLong());
		verify(metadataSingleton, times(books.size())).setCurrent(anyLong());
		verify(metadataSingleton, times(books.size())).increase();
	}

	@Test
	public void testStart_MetadataSingletonNotRunning_ShouldStopProcessing() {
		// Given
		boolean override = true;

		List<BookMongoEntity> books = new ArrayList<>();
		// Agregar libros a la lista 'books' para simular resultados del repositorio
		// ...

		when(bookRepository.count()).thenReturn((long) books.size());
		when(bookRepository.findAll(null, 0, 100, "id", "asc")).thenReturn(books);
		when(metadataSingleton.isRunning()).thenReturn(false);

		// When
		startFillBooksMetadataUseCase.start(override);

		// Then
		// Verificar que el método findAll() del repositorio no es llamado porque metadataSingleton.isRunning() es falso
		verify(bookRepository, never()).findAll(null, 0, 100, "id", "asc");
		// Verificar que el método executeAndWait() del commandBus tampoco es llamado
		verify(commandBus, never()).executeAndWait(any(FindBookMetadataCommand.class));
	}

	// Otras pruebas para otros casos

}
