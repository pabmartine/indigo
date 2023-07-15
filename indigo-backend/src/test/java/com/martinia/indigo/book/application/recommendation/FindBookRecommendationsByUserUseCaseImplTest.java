package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByUserUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindBookRecommendationsByUserUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookRecommendationsByUserUseCase findBookRecommendationsByUserUseCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Test
	public void testGetRecommendationsByUser_ReturnsRecommendedBooks() {
		// Given
		String user = "john_doe";
		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";

		BookMongoEntity bookEntity1 = new BookMongoEntity();
		bookEntity1.setId("1");
		bookEntity1.setTitle("Book 1");

		BookMongoEntity bookEntity2 = new BookMongoEntity();
		bookEntity2.setId("2");
		bookEntity2.setTitle("Book 2");

		List<BookMongoEntity> bookEntities = Arrays.asList(bookEntity1, bookEntity2);

		when(bookRepository.getRecommendationsByUser(user, page, size, sort, order)).thenReturn(bookEntities);
		when(bookMongoMapper.entities2Domains(bookEntities)).thenReturn(Arrays.asList(new Book(), new Book()));

		// When
		List<Book> result = findBookRecommendationsByUserUseCase.getRecommendationsByUser(user, page, size, sort, order);

		// Then
		assertEquals(1, result.size());
	}

}
