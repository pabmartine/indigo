package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByBookUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class FindBookRecommendationsByBookUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private ConfigurationRepository configurationRepository;

	@Resource
	private FindBookRecommendationsByBookUseCase findBookRecommendationsByBookUseCase;

	@Test
	public void testGetRecommendationsByBook() {
		// Given
		List<String> recommendations = Arrays.asList("123", "456");
		List<String> languages = Arrays.asList("English", "Spanish");
		int numRecommendations = 5;
		List<BookMongoEntity> expectedRecommendations = Arrays.asList(new BookMongoEntity(), new BookMongoEntity());

		// Mock the behavior of configurationRepository.findByKey()
		when(configurationRepository.findByKey("books.recommendations")).thenReturn(
				Optional.of(ConfigurationMongoEntity.builder().key("books.recommendations").value(String.valueOf(numRecommendations)).build()));

		// Mock the behavior of bookRepository.getRecommendationsByBook()
		when(bookRepository.getRecommendationsByBook(recommendations, languages, numRecommendations)).thenReturn(expectedRecommendations);

		// When
		List<Book> actualRecommendations = findBookRecommendationsByBookUseCase.getRecommendationsByBook(recommendations, languages);

		// Then
		assertNotNull(actualRecommendations);
		assertEquals(expectedRecommendations.size(), actualRecommendations.size());
		assertTrue(actualRecommendations.containsAll(expectedRecommendations));
	}
}
