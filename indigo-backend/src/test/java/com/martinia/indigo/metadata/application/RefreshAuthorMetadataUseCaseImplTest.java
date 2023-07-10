package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshAuthorMetadataUseCase;
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
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

public class RefreshAuthorMetadataUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private AuthorRepository authorRepository;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private DataUtils dataUtils;

	@Resource
	private RefreshAuthorMetadataUseCase refreshAuthorMetadataUseCase;

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

	@BeforeEach
	@SneakyThrows
	void init() {
		Mockito.when(dataUtils.getData(any())).thenReturn(null);
		Mockito.when(findGoogleBooksBookUseCase.findBook(any(), any())).thenReturn(null);
	}

	@Test
	public void testFindAuthorMetadata_WhenAuthorExists_ThenReturnAuthorMetadata() {
		// Given
		String sort = "John Doe";
		String lang = "en";

		Author author = new Author();
		author.setName(sort);
		author.setSort(sort);

		Author expectedAuthor = new Author();
		expectedAuthor.setName(sort);
		expectedAuthor.setSort(sort);

		Mockito.when(authorRepository.findBySort(sort)).thenReturn(Optional.of(author));
		Mockito.when(dataUtils.getData(any())).thenReturn(null);
		Mockito.when(bookRepository.findAll(any(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(Collections.emptyList());
		// When
		Optional<Author> result = refreshAuthorMetadataUseCase.findAuthorMetadata(sort, lang);

		// Then
		Assertions.assertTrue(result.isPresent());
		super.assertRecursively(expectedAuthor, result.get(), "lastMetadataSync");
		Mockito.verify(authorRepository).findBySort(sort);
		Mockito.verify(authorRepository).update(any());
	}

	@Test
	public void testFindAuthorMetadata_WhenAuthorDoesNotExist_ThenReturnEmptyOptional() {
		// Given
		String sort = "John Doe";
		String lang = "en";

		Mockito.when(authorRepository.findBySort(sort)).thenReturn(Optional.empty());

		// When
		Optional<Author> result = refreshAuthorMetadataUseCase.findAuthorMetadata(sort, lang);

		// Then
		Assertions.assertTrue(result.isEmpty());
		Mockito.verify(authorRepository).findBySort(sort);
		Mockito.verifyNoMoreInteractions(authorRepository);
	}
}
