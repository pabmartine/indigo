package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByPathUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.when;

public class FindBookByPathUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookByPathUseCase findBookByPathUseCase;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void givenExistingBookPath_whenFindByPath_thenReturnBook() throws Exception {
		// Given
		String bookPath = "/path/to/book";
		Book expectedBook = new Book();
		when(bookRepository.findByPath(bookPath)).thenReturn(Optional.of(expectedBook));

		// When
		Optional<Book> result = findBookByPathUseCase.findByPath(bookPath);

		// Then
		Assertions.assertEquals(expectedBook, result.orElse(null));
	}

	@Test
	public void givenNonExistingBookPath_whenFindByPath_thenReturnEmptyOptional() throws Exception {
		// Given
		String bookPath = "/non/existing/path";
		when(bookRepository.findByPath(bookPath)).thenReturn(Optional.empty());

		// When
		Optional<Book> result = findBookByPathUseCase.findByPath(bookPath);

		// Then
		Assertions.assertTrue(result.isEmpty());
	}
}
