package com.martinia.indigo.book.application.serie;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.serie.FindBooksBySerieUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;

public class FindBooksBySerieUseCaseImplTest extends BaseIndigoTest{

	@Resource
	private FindBooksBySerieUseCase findBooksBySerieUseCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Test
	public void testGetSerie_ReturnsEmptyListWhenNoBooksFound() {
		// Given
		String serie = "Serie 1";
		List<String> languages = Arrays.asList("English", "Spanish");

		when(bookRepository.getSerie(serie, languages)).thenReturn(Collections.emptyList());

		// When
		List<Book> result = findBooksBySerieUseCase.getSerie(serie, languages);

		// Then
		assertEquals(Collections.emptyList(), result);
	}

	@Test
	public void testGetSerie_ReturnsListOfBooksWhenBooksFound() {
		// Given
		String serie = "Serie 1";
		List<String> languages = Arrays.asList("English", "Spanish");

		BookMongoEntity book1 = new BookMongoEntity();
		book1.setId("1");
		book1.setTitle("Book 1");

		BookMongoEntity book2 = new BookMongoEntity();
		book2.setId("2");
		book2.setTitle("Book 2");

		List<BookMongoEntity> bookEntities = Arrays.asList(book1, book2);

		when(bookRepository.getSerie(serie, languages)).thenReturn(bookEntities);
		when(bookMongoMapper.entities2Domains(bookEntities)).thenReturn(Arrays.asList(new Book(), new Book()));

		// When
		List<Book> result = findBooksBySerieUseCase.getSerie(serie, languages);

		// Then
		assertEquals(2, result.size());
	}
}
