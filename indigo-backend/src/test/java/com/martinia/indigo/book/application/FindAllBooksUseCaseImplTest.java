package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.FindAllBooksUseCase;
import com.martinia.indigo.domain.model.Search;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

class FindAllBooksUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindAllBooksUseCase findAllBooksUseCase;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void testFindAll() {
		// Given
		Search search = new Search();
		int page = 1;
		int size = 10;
		String sort = "title";
		String order = "asc";
		List<Book> expectedBooks = new ArrayList<>();
		// Add some sample books to the expectedBooks list

		// Mock the behavior of the bookRepository
		when(bookRepository.findAll(search, page, size, sort, order)).thenReturn(expectedBooks);

		// When
		List<Book> actualBooks = findAllBooksUseCase.findAll(search, page, size, sort, order);

		// Then
		// Assert the actualBooks list matches the expectedBooks list
		// Add your assertions here
		assertRecursively(expectedBooks, actualBooks);
	}

}