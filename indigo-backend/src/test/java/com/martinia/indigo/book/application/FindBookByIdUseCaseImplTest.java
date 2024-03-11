package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByIdUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class FindBookByIdUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookByIdUseCase findBookByIdUseCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Test
	public void testFindById_WithExistingBook_ReturnsOptionalOfBook() {
		// Given
		String bookId = "1";

		BookMongoEntity bookEntity = new BookMongoEntity();
		bookEntity.setId(bookId);
		bookEntity.setTitle("Book 1");

		when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
		when(bookMongoMapper.entity2Domain(bookEntity)).thenReturn(new Book());

		// When
		Optional<Book> result = findBookByIdUseCase.findById(bookId);

		// Then
		assertTrue(result.isPresent());

	}

	@Test
	public void testFindById_WithNonExistingBook_ReturnsEmptyOptional() {
		// Given
		String bookId = "1";

		when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

		// When
		Optional<Book> result = findBookByIdUseCase.findById(bookId);

		// Then
		assertFalse(result.isPresent());
	}
}
