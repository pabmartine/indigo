package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByUserUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.util.UtilComponent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class FindBookRecommendationsByUserUseCaseImplTest extends BaseIndigoTest {
	@MockBean
	private BookMongoRepository bookRepository;

	@MockBean
	private UtilComponent utilComponent;

	@Resource
	private FindBookRecommendationsByUserUseCase findBookRecommendationsByUserUseCase;

	@Test
	public void testGetRecommendationsByUser() {
		// Given
		String user = "exampleUser";
		int page = 1;
		int size = 10;
		String sort = "title";
		String order = "asc";
		List<BookMongoEntity> expectedRecommendations = Arrays.asList(new BookMongoEntity(), new BookMongoEntity());

		// Mock the behavior of bookRepository.getRecommendationsByUser()
		when(bookRepository.getRecommendationsByUser(user, page, size, sort, order)).thenReturn(expectedRecommendations);

		// When
		List<Book> actualRecommendations = findBookRecommendationsByUserUseCase.getRecommendationsByUser(user, page, size, sort, order);

		// Then
		assertNotNull(actualRecommendations);
		assertEquals(expectedRecommendations.size(), actualRecommendations.size());
		assertTrue(actualRecommendations.containsAll(expectedRecommendations));
	}
}