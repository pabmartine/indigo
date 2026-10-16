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
		when(dataUtils.getData(contains("q=%22The+Hobbit%22"))).thenReturn("""
				{"docs":[{"key":"/works/OL27448W","edition_key":["OL5171337M"],"title":"The Hobbit","author_name":["J. R. R. Tolkien"],"ratings_average":4.28,"ratings_count":321}]}
				""");

		BookMetadataResult result = useCase.findBook("The Hobbit", List.of("J. R. R. Tolkien"));

		assertThat(result.getRatingAverage()).isEqualTo(4.28F);
		assertThat(result.getRatingsCount()).isEqualTo(321L);
		assertThat(result.getProvider()).isEqualTo(ProviderEnum.OPEN_LIBRARY.name());
		assertThat(result.getOpenLibraryWorkId()).isEqualTo("OL27448W");
		assertThat(result.getOpenLibraryEditionId()).isNull();
	}

	@Test
	void shouldIgnoreAResultFromAnotherAuthor() {
		when(dataUtils.getData(contains("q=%22The+Hobbit%22"))).thenReturn("""
				{"docs":[{"title":"The Hobbit","author_name":["Another Author"],"ratings_average":4.28}]}
				""");

		assertThat(useCase.findBook("The Hobbit", List.of("J. R. R. Tolkien"))).isNull();
	}

    @Test void titlePrefixDoesNotMatchASequel() {
        when(dataUtils.getData(contains("q=%22Dune%22"))).thenReturn("""
            {"docs":[{"key":"/works/OL1W","title":"Dune Messiah","author_name":["Frank Herbert"],"ratings_average":4.5}]}
            """);
        assertThat(useCase.findBook("Dune", List.of("Frank Herbert"))).isNull();
    }
    @Test void titleMatchWithoutRatingsStillKeepsTheWorkIdentity() {
        when(dataUtils.getData(contains("q=%22Dune%22"))).thenReturn("""
            {"docs":[{"key":"/works/OL1W","title":"Dune","author_name":["Frank Herbert"]}]}
            """);
        var identity = useCase.findBook("Dune", List.of("Frank Herbert"));
        assertThat(identity.getOpenLibraryWorkId()).isEqualTo("OL1W");
        assertThat(identity.getRatingAverage()).isNull();
    }

    @Test void laterMatchingRatingIsPreferredToAnUnratedIdentity() {
        when(dataUtils.getData(contains("q=%22Dune%22"))).thenReturn("""
            {"docs":[{"key":"/works/OL1W","title":"Dune","author_name":["Frank Herbert"]},
             {"key":"/works/OL1W","title":"Dune","author_name":["Frank Herbert"],"ratings_average":4.5,"ratings_count":"broken"}]}
            """);
        var result = useCase.findBook("Dune", List.of("Frank Herbert"));
        assertThat(result.getRatingAverage()).isEqualTo(4.5F);
        assertThat(result.getRatingsCount()).isNull();
    }
    @Test void invalidProviderResponseIsNotANegativeMatch() {
        when(dataUtils.getData(contains("q=%22Dune%22"))).thenReturn("{\"error\":\"unavailable\"}");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> useCase.findBook("Dune", List.of()))
                .isInstanceOf(IllegalStateException.class);
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
    @Test void translatedEditionCanMatchWhenWorkUsesItsOriginalTitle() {
        when(dataUtils.getData(contains("q=%22El+Hobbit%22"))).thenReturn("""
            {"docs":[{"key":"/works/OL1W","title":"The Hobbit","author_name":["J. R. R. Tolkien"],
             "ratings_average":4.5,"ratings_count":100,"editions":{"docs":[{"key":"/books/OL9M","title":"El Hobbit"}]}}]}
            """);
        var result = useCase.findBook("El Hobbit", List.of("J. R. R. Tolkien"));
        var url = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(dataUtils).getData(url.capture());
        assertThat(url.getValue()).contains("q=%22El+Hobbit%22", "editions.title").doesNotContain("?title=");
        assertThat(result.getRatingAverage()).isEqualTo(4.5F);
        assertThat(result.getOpenLibraryEditionId()).isEqualTo("OL9M");
        assertThat(useCase.findBook("El Hobbit", List.of("Another Author"))).isNull();
    }
    @Test void aMatchingAuthorDoesNotMakeAnUnrelatedTranslatedEditionValid() {
        when(dataUtils.getData(contains("q=%22El+Hobbit%22"))).thenReturn("""
            {"docs":[{"key":"/works/OL1W","title":"The Lord of the Rings","author_name":["J. R. R. Tolkien"],
             "ratings_average":4.5,"editions":{"docs":[{"key":"/books/OL9M","title":"El señor de los anillos"}]}}]}
            """);
        assertThat(useCase.findBook("El Hobbit", List.of("J. R. R. Tolkien"))).isNull();
    }

}
