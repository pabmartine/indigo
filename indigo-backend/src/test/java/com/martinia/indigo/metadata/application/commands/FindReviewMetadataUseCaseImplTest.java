package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.common.infrastructure.api.mappers.ReviewDtoMapper;
import com.martinia.indigo.common.infrastructure.mongo.mappers.ReviewMongoMapper;
import com.martinia.indigo.metadata.domain.ports.adapters.amazon.FindAmazonReviewsPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsReviewsPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class FindReviewMetadataUseCaseImplTest extends BaseIndigoTest {

	@Autowired
	private FindReviewMetadataUseCaseImpl findReviewMetadataUseCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private FindGoodReadsReviewsPort findGoodReadsReviewsPort;

	@MockBean
	private FindAmazonReviewsPort findAmazonReviewsPort;

	@MockBean
	private ReviewDtoMapper reviewDtoMapper;

	@MockBean
	private ReviewMongoMapper reviewMongoMapper;

	@Test
	public void testFind_OverrideFalseAndLastExecutionNotExpired_ShouldNotFindAndSaveReviews() {
		// Given
		String bookId = "example_book_id";
		boolean override = false;
		String lang = "en";
		Book book = new Book();
		book.setId(bookId);
		book.setTitle("example_book_title");
		book.setAuthors(Collections.singletonList("example_author"));
		List<ReviewMongo> reviews = Collections.singletonList(new ReviewMongo());
		// Configurar el comportamiento esperado del bookRepository.findById()
		// ...

		when(bookRepository.findById(bookId)).thenReturn(Optional.of(new BookMongoEntity()));
		when(reviewMongoMapper.domains2Entities(anyList())).thenReturn(reviews);
		when(reviewMongoMapper.domains2Entities(anyList())).thenReturn(Collections.singletonList(new ReviewMongo()));
		when(findAmazonReviewsPort.getReviews(anyString(), anyList())).thenReturn(Collections.emptyList());

		// When
		findReviewMetadataUseCase.find(bookId, override, System.currentTimeMillis(), lang);

		// Then
		// Verificar que se llama al método findById() del bookRepository
		verify(bookRepository, times(1)).findById(bookId);
		// Verificar que el método getReviews() del findGoodReadsReviewsPort no es llamado
		verify(findGoodReadsReviewsPort, never()).getReviews(anyString(), anyString(), anyList());
		// Verificar que el método getReviews() del findAmazonReviewsPort no es llamado
		verify(findAmazonReviewsPort, never()).getReviews(anyString(), anyList());
		// Verificar que el método save() del bookRepository no es llamado
		verify(bookRepository, atLeast(1)).save(any());
	}

	// Otras pruebas para otros casos

}
