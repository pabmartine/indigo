package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.common.configuration.domain.model.Configuration;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;
import com.martinia.indigo.domain.util.DataUtils;
import com.martinia.indigo.metadata.domain.service.RefreshBookMetadataUseCase;
import com.martinia.indigo.ports.out.metadata.AmazonService;
import com.martinia.indigo.ports.out.metadata.GoodReadsService;
import com.martinia.indigo.ports.out.metadata.GoogleBooksService;
import com.martinia.indigo.ports.out.metadata.WikipediaService;
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
	private BookRepository mockBookRepository;

	@MockBean
	private ConfigurationRepository mockConfigurationRepository;

	@MockBean
	private DataUtils dataUtils;

	@MockBean
	private GoodReadsService goodReadsService;

	@MockBean
	private WikipediaService wikipediaService;

	@MockBean
	private GoogleBooksService googleBooksService;

	@MockBean
	private AmazonService amazonService;

	@Resource
	private RefreshBookMetadataUseCase refreshBookMetadataUseCase;

	@BeforeEach
	@SneakyThrows
	void init() {
		Mockito.when(mockConfigurationRepository.findByKey(any())).thenReturn(Optional.of(new Configuration("key", "value")));
		Mockito.when(dataUtils.getData(any())).thenReturn(null);
		Mockito.when(googleBooksService.findBook(any(), any())).thenReturn(null);
	}

	@Test
	public void testFindBookMetadata_WhenBookExists_ThenReturnBookMetadata() {
		// Given
		String path = "/path/to/book";

		Book book = new Book();
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

		Book book = new Book();
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
