package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.domain.model.Search;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;

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
		when(bookRepository.countBooks(search)).thenReturn(expectedCount);

		// When
		long actualCount = countAllUseCase.count(search);

		// Then
		assertEquals(expectedCount, actualCount);
	}

	@Test
	public void testCount_WithNullSearch_ReturnsZero() {
		// Given
		Search nullSearch = null;
		long expectedCount = 0L;

		when(bookRepository.countBooks(nullSearch)).thenReturn(expectedCount);

		// When
		long actualCount = countAllUseCase.count(nullSearch);

		// Then
		assertEquals(expectedCount, actualCount);
	}

	@Test
	public void testCount_WithSearchText_ReturnsFilteredCount() {
		// Given
		Search searchWithText = new Search();
		searchWithText.setTitle("fantasy");
		long expectedCount = 15L;

		when(bookRepository.countBooks(searchWithText)).thenReturn(expectedCount);

		// When
		long actualCount = countAllUseCase.count(searchWithText);

		// Then
		assertEquals(expectedCount, actualCount);
	}

	@Test
	public void testCount_WithNoResults_ReturnsZero() {
		// Given
		Search searchWithNoResults = new Search();
		searchWithNoResults.setTitle("nonexistent");
		long expectedCount = 0L;

		when(bookRepository.countBooks(searchWithNoResults)).thenReturn(expectedCount);

		// When
		long actualCount = countAllUseCase.count(searchWithNoResults);

		// Then
		assertEquals(expectedCount, actualCount);
	}

}