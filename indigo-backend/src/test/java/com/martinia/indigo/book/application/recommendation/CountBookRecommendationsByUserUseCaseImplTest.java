package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.CountBookRecommendationsByUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CountBookRecommendationsByUserUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private CountBookRecommendationsByUserUseCase countBookRecommendationsByUserUseCase;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void testCountRecommendationsByUser() {
		// Given
		String user = "exampleUser";
		long expectedCount = 5L;

		// Mock the behavior of the bookRepository
		when(bookRepository.countRecommendationsByUser(user)).thenReturn(expectedCount);

		// When
		long actualCount = countBookRecommendationsByUserUseCase.countRecommendationsByUser(user);

		// Then
		assertEquals(expectedCount, actualCount);

	}

}