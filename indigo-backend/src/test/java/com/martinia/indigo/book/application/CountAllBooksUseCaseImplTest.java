package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.model.Search;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CountAllBooksUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private CountAllBooksUseCaseImpl countAllUseCase;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void testCount() {
		// Given
		Search search = new Search();
		long expectedCount = 10L;

		// Mock the behavior of the bookRepository
		when(bookRepository.count(search)).thenReturn(expectedCount);

		// When
		long actualCount = countAllUseCase.count(search);

		// Then
		assertEquals(expectedCount, actualCount);
	}

}