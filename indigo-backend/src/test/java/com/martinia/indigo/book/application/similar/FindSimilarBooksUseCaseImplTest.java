package com.martinia.indigo.book.application.similar;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindSimilarBooksUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindSimilarBooksUseCaseImpl findSimilarBooksUseCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Test
	public void testGetSimilar_ReturnsSimilarBooks() {
		// Given
		List<String> similar = Arrays.asList("book1", "book2");
		List<String> languages = Collections.singletonList("English");

		BookMongoEntity bookEntity1 = new BookMongoEntity();
		bookEntity1.setId("1");
		bookEntity1.setTitle("Book 1");

		BookMongoEntity bookEntity2 = new BookMongoEntity();
		bookEntity2.setId("2");
		bookEntity2.setTitle("Book 2");

		List<BookMongoEntity> bookEntities = Arrays.asList(bookEntity1, bookEntity2);

		when(bookRepository.getSimilar(similar, languages)).thenReturn(bookEntities);
		when(bookMongoMapper.entities2Domains(bookEntities)).thenReturn(Arrays.asList(new Book(), new Book()));

		// When
		List<Book> result = findSimilarBooksUseCase.getSimilar(similar, languages);

		// Then
		assertEquals(2, result.size());
	}

	@Test
	public void testGetSimilar_ReturnsEmptyListWhenNoSimilarBooks() {
		// Given
		List<String> similar = Arrays.asList("book1", "book2");
		List<String> languages = Collections.singletonList("English");

		when(bookRepository.getSimilar(similar, languages)).thenReturn(Collections.emptyList());

		// When
		List<Book> result = findSimilarBooksUseCase.getSimilar(similar, languages);

		// Then
		assertEquals(0, result.size());
	}
}
