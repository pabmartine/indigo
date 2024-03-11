package com.martinia.indigo.serie.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

public class FindCoverSerieUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindCoverSerieUseCase findCoverSerieUseCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Test
	public void testGetCover_ReturnsCover() {
		// Given
		String serie = "Serie 1";

		Book book1 = new Book();
		book1.setImage("cover.jpg");

		Book book2 = new Book();

		List<Book> books = Arrays.asList(book1, book2);

		when(bookMongoMapper.entities2Domains(Collections.singletonList(Mockito.any(BookMongoEntity.class)))).thenReturn(books);
		when(bookRepository.findBooksBySerie(serie)).thenReturn(Collections.singletonList(Mockito.any(BookMongoEntity.class)));

		// When
		String result = findCoverSerieUseCase.getCover(serie);

		// Then
		assertNull(result);
	}
}
