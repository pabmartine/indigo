package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByPathUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class FindBookByPathUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookByPathUseCase findBookByPathUseCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Test
	public void testFindByPath_WithExistingBook_ReturnsOptionalOfBook() {
		// Given
		String path = "/books/1";

		BookMongoEntity bookEntity = new BookMongoEntity();
		bookEntity.setId("1");
		bookEntity.setTitle("Book 1");

		when(bookRepository.findByPath(path)).thenReturn(Optional.of(bookEntity));
		when(bookMongoMapper.entity2Domain(bookEntity)).thenReturn(new Book());

		// When
		Optional<Book> result = findBookByPathUseCase.findByPath(path);

		// Then
		assertTrue(result.isPresent());
	}

	@Test
	public void testFindByPath_WithNonExistingBook_ReturnsEmptyOptional() {
		// Given
		String path = "/books/1";

		when(bookRepository.findByPath(path)).thenReturn(Optional.empty());

		// When
		Optional<Book> result = findBookByPathUseCase.findByPath(path);

		// Then
		assertFalse(result.isPresent());
	}

	@Test
	public void testFindByPath_WithNullPath_ReturnsEmptyOptional() {
		// Given
		String nullPath = null;

		when(bookRepository.findByPath(nullPath)).thenReturn(Optional.empty());

		// When
		Optional<Book> result = findBookByPathUseCase.findByPath(nullPath);

		// Then
		assertFalse(result.isPresent());
	}

	@Test
	public void testFindByPath_WithEmptyPath_ReturnsEmptyOptional() {
		// Given
		String emptyPath = "";

		when(bookRepository.findByPath(emptyPath)).thenReturn(Optional.empty());

		// When
		Optional<Book> result = findBookByPathUseCase.findByPath(emptyPath);

		// Then
		assertFalse(result.isPresent());
	}

	@Test
	public void testFindByPath_WithValidPath_ReturnsBook() {
		// Given
		String path = "/library/books/fantasy/book.epub";

		BookMongoEntity bookEntity = new BookMongoEntity();
		bookEntity.setId("123");
		bookEntity.setTitle("Fantasy Book");
		bookEntity.setPath(path);

		Book expectedBook = new Book();
		expectedBook.setId("123");
		expectedBook.setTitle("Fantasy Book");
		expectedBook.setPath(path);

		when(bookRepository.findByPath(path)).thenReturn(Optional.of(bookEntity));
		when(bookMongoMapper.entity2Domain(bookEntity)).thenReturn(expectedBook);

		// When
		Optional<Book> result = findBookByPathUseCase.findByPath(path);

		// Then
		assertTrue(result.isPresent());
		assertEquals(path, result.get().getPath());
		assertEquals("Fantasy Book", result.get().getTitle());
	}
}
