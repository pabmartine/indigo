package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.DeleteBookUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteBookUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private BookRepository bookRepository;

	@Resource
	private DeleteBookUseCase deleteBookUseCase;

	@Test
	public void testDeleteBook() {
		// Given
		String bookId = "book123";

		// Simular que el repositorio devuelve un libro con el ID dado
		BookMongoEntity book = BookMongoEntity.builder().id(bookId).build();
		when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

		// When
		deleteBookUseCase.delete(bookId);

		// Then
		// Verificar que el método delete del repositorio es llamado con el libro correcto
		verify(bookRepository).delete(book);
	}

	@Test
	public void testDeleteNonExistentBook() {
		// Given
		String bookId = "nonExistentBookId";

		// Simular que el repositorio no encuentra ningún libro con el ID dado
		when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

		// When
		deleteBookUseCase.delete(bookId);

		// Then
		// Verificar que el método delete del repositorio no es llamado, ya que no hay libro para eliminar
		verify(bookRepository, never()).delete(Mockito.any(BookMongoEntity.class));
	}

	// Otras pruebas para otros casos

}
