package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindOpenLibraryBookUseCaseImplTest {

	@Mock
	private DataUtils dataUtils;

	@InjectMocks
	private FindOpenLibraryBookUseCaseImpl useCase;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(useCase, "endpoint",
				"https://openlibrary.org/search.json?title=$title&author=$author");
		ReflectionTestUtils.setField(useCase, "isbnEndpoint",
				"https://openlibrary.org/search.json?isbn=$isbn");
	}

	@Test
	void shouldReturnRatingForMatchingBookAndAuthor() {
		when(dataUtils.getData(contains("title=The+Hobbit"))).thenReturn("""
				{"docs":[{"key":"/works/OL27448W","edition_key":["OL5171337M"],"title":"The Hobbit","author_name":["J. R. R. Tolkien"],"ratings_average":4.28,"ratings_count":321}]}
				""");

		BookMetadataResult result = useCase.findBook("The Hobbit", List.of("J. R. R. Tolkien"));

		assertThat(result.getRatingAverage()).isEqualTo(4.28F);
		assertThat(result.getRatingsCount()).isEqualTo(321L);
		assertThat(result.getProvider()).isEqualTo(ProviderEnum.OPEN_LIBRARY.name());
		assertThat(result.getOpenLibraryWorkId()).isEqualTo("OL27448W");
		assertThat(result.getOpenLibraryEditionId()).isEqualTo("OL5171337M");
	}

	@Test
	void shouldIgnoreAResultFromAnotherAuthor() {
		when(dataUtils.getData(contains("title=The+Hobbit"))).thenReturn("""
				{"docs":[{"title":"The Hobbit","author_name":["Another Author"],"ratings_average":4.28}]}
				""");

		assertThat(useCase.findBook("The Hobbit", List.of("J. R. R. Tolkien"))).isNull();
	}

	@Test
	void shouldPreferExactIsbnLookup() {
		when(dataUtils.getData(contains("isbn=9780261102217"))).thenReturn("""
				{"docs":[{"key":"/works/OL27448W","edition_key":["OL5171337M"],"ratings_average":4.31,"ratings_count":500}]}
				""");

		BookMetadataResult result = useCase.findBook(BookMetadataQuery.builder()
				.title("A translated title")
				.authors(List.of("An author spelling that differs"))
				.isbn13(List.of("9780261102217"))
				.build());

		assertThat(result).isNotNull();
		assertThat(result.getOpenLibraryWorkId()).isEqualTo("OL27448W");
		assertThat(result.getRatingAverage()).isEqualTo(4.31F);
	}

	@Test
	void shouldKeepExactIsbnMappingWhenTheWorkHasNoRatings() {
		when(dataUtils.getData(contains("isbn=9780261102217"))).thenReturn("""
				{"docs":[{"key":"/works/OL27448W","edition_key":["OL5171337M"]}]}
				""");

		BookMetadataResult result = useCase.findBook(BookMetadataQuery.builder()
				.isbn13(List.of("9780261102217"))
				.build());

		assertThat(result).isNotNull();
		assertThat(result.getOpenLibraryWorkId()).isEqualTo("OL27448W");
		assertThat(result.getOpenLibraryEditionId()).isEqualTo("OL5171337M");
		assertThat(result.getRatingAverage()).isNull();
	}
}
