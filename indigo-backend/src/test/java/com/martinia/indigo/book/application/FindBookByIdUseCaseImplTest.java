package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.FindBookByIdUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class FindBookByIdUseCaseImplTest {

	@Resource
	private FindBookByIdUseCase findBookByIdUseCase;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void givenExistingBookId_whenFindById_thenReturnBook() throws Exception {
		// Given
		String bookId = "1";
		Book expectedBook = new Book();
		when(bookRepository.findById(bookId)).thenReturn(Optional.of(expectedBook));

		// When
		Optional<Book> result = findBookByIdUseCase.findById(bookId);

		// Then
		Assertions.assertEquals(expectedBook, result.orElse(null));
	}

	@Test
	public void givenNonExistingBookId_whenFindById_thenReturnEmptyOptional() throws Exception {
		// Given
		String bookId = "2";
		when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

		// When
		Optional<Book> result = findBookByIdUseCase.findById(bookId);

		// Then
		Assertions.assertTrue(result.isEmpty());
	}

}
