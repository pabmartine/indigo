package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationMongoRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshBookMetadataUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.amazon.FindAmazonReviewsUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsAuthorUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsBookUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsReviewsUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.google.FindGoogleBooksBookUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorUseCase;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

public class RefreshBookMetadataUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private BookMongoRepository mockBookRepository;

	@MockBean
	private ConfigurationMongoRepository mockConfigurationRepository;

	@MockBean
	private DataUtils dataUtils;

	@MockBean
	private FindGoodReadsBookUseCase findGoodReadsBookUseCase;

	@MockBean
	private FindGoodReadsAuthorUseCase findGoodReadsAuthorUseCase;

	@MockBean
	private FindGoodReadsReviewsUseCase findGoodReadsReviewsUseCase;

	@MockBean
	private FindWikipediaAuthorUseCase findWikipediaAuthorUseCase;

	@MockBean
	private FindGoogleBooksBookUseCase findGoogleBooksBookUseCase;

	@MockBean
	private FindAmazonReviewsUseCase findAmazonReviewsUseCase;

	@Resource
	private RefreshBookMetadataUseCase refreshBookMetadataUseCase;

	@BeforeEach
	@SneakyThrows
	void init() {
		Mockito.when(mockConfigurationRepository.findByKey(any())).thenReturn(Optional.of(
				ConfigurationMongoEntity.builder().key("goodreads.key").value("123456").build()));
		Mockito.when(dataUtils.getData(any())).thenReturn(null);
		Mockito.when(findGoogleBooksBookUseCase.findBook(any(), any())).thenReturn(null);
	}

	@Test
	public void testFindBookMetadata_WhenBookExists_ThenReturnBookMetadata() {
		// Given
		String path = "/path/to/book";

		BookMongoEntity book = new BookMongoEntity();
		book.setTitle("title");
		book.setPath(path);

		final List<String> authors = new ArrayList<>();
		authors.add("authors");
		book.setAuthors(authors);

		Book expectedBook = new Book();
		expectedBook.setPath(path);

		Mockito.when(mockBookRepository.findByPath(path)).thenReturn(Optional.of(book));

		// When
		Optional<Book> result = refreshBookMetadataUseCase.findBookMetadata(path);

		// Then
		Assertions.assertTrue(result.isPresent());

		Mockito.verify(mockBookRepository).findByPath(path);
		Mockito.verify(mockBookRepository).save(any());
	}

	@Test
	public void testFindBookMetadata_WhenBookDoesNotExist_ThenReturnEmptyOptional() {
		// Given
		String path = "/path/to/nonexistent/book";

		Mockito.when(mockBookRepository.findByPath(path)).thenReturn(Optional.empty());

		// When
		Optional<Book> result = refreshBookMetadataUseCase.findBookMetadata(path);

		// Then
		Assertions.assertTrue(result.isEmpty());
		Mockito.verify(mockBookRepository).findByPath(path);
		Mockito.verifyNoMoreInteractions(mockBookRepository);
	}

	@Test
	public void testFindBookMetadata_WhenGoodreadsKeyIsNull_ThenReturnEmptyOptional() {
		// Given
		String path = "/path/to/book";

		BookMongoEntity book = new BookMongoEntity();
		book.setPath(path);

		Mockito.when(mockBookRepository.findByPath(path)).thenReturn(Optional.of(book));
		Mockito.when(mockConfigurationRepository.findByKey("goodreads.key")).thenReturn(Optional.empty());

		// When
		Optional<Book> result = refreshBookMetadataUseCase.findBookMetadata(path);

		// Then
		Assertions.assertTrue(result.isPresent());
		Mockito.verify(mockBookRepository).findByPath(path);
		Mockito.verifyNoMoreInteractions(mockConfigurationRepository);
	}
}
