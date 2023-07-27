package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EditBookUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Resource
	private EditBookUseCaseImpl editBookUseCase;

	@Test
	public void testEditBook() {
		// Given
		Book book = new Book();
		book.setId("book-id");
		book.setTitle("New Title");
		book.setAuthors(List.of("New Author"));

		BookMongoEntity existingEntity = new BookMongoEntity();
		existingEntity.setId("book-id");
		existingEntity.setTitle("Old Title");
		existingEntity.setAuthors(List.of("Old Author"));

		when(bookRepository.findById("book-id")).thenReturn(Optional.of(existingEntity));
		when(bookMongoMapper.domain2Entity(any(Book.class))).thenReturn(existingEntity);

		// When
		editBookUseCase.edit(book);

		// Then
		verify(bookRepository).findById("book-id");
		verify(bookMongoMapper).domain2Entity(book);
		verify(bookRepository).save(existingEntity);
	}

	@Test
	public void testEditBook_NotFound() {
		// Given
		Book book = new Book();
		book.setId("book-id");
		book.setTitle("New Title");
		book.setAuthors(List.of("New Author"));

		when(bookRepository.findById("book-id")).thenReturn(Optional.empty());

		// When
		editBookUseCase.edit(book);

		// Then
		verify(bookRepository).findById("book-id");
		verify(bookRepository, never()).save(any());
	}

	// Otras pruebas para otros casos

}
