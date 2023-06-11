package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.FindBookByPathUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class FindBookByPathUseCaseImplTest {

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
