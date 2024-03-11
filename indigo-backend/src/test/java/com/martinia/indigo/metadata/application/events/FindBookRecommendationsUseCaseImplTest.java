package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FindBookRecommendationsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindBookRecommendationsUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookRecommendationsUseCase useCase;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void testFindBookRecommendations() {
		// Given
		String bookId = "testBookId";
		BookMongoEntity mockBook = new BookMongoEntity();
		List<BookMongoEntity> mockRecommendations = new ArrayList<>();
		mockRecommendations.add(mockBook);

		when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
		when(bookRepository.getRecommendationsByBook(mockBook)).thenReturn(mockRecommendations);

		// When
		useCase.findBookRecommendations(bookId);

		// Then
		verify(bookRepository).save(any());
		assertEquals(mockRecommendations.size(), mockBook.getRecommendations().size());

	}

	@Test
	public void testFindBookRecommendationsWithNonExistingBook() {
		// Given
		String bookId = "nonExistingBookId";

		when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

		// When
		useCase.findBookRecommendations(bookId);

		// Then
		verify(bookRepository, times(1)).findById(any());
	}

}