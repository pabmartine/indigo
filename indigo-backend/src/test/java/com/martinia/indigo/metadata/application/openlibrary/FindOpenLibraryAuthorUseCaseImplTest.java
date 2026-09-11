package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindOpenLibraryAuthorUseCaseImplTest {

	@Mock
	private DataUtils dataUtils;
	@Mock
	private com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation spanishTranslation;

	@InjectMocks
	private FindOpenLibraryAuthorUseCaseImpl useCase;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(useCase, "authorsEndpoint", "https://openlibrary.org/search/authors.json?q=$subject");
		ReflectionTestUtils.setField(useCase, "authorInfoEndpoint", "https://openlibrary.org/authors/$id.json");
		ReflectionTestUtils.setField(useCase, "authorImageEndpoint", "https://covers.openlibrary.org/a/olid/$id-L.jpg?default=false");
	}

	@Test
	void shouldReturnBiographyAndImageForExactAuthor() {
		when(dataUtils.getData("https://openlibrary.org/search/authors.json?q=J.+R.+R.+Tolkien"))
				.thenReturn("{\"docs\":[{\"key\":\"OL26320A\",\"name\":\"J. R. R. Tolkien\"}]}");
		when(dataUtils.getData("https://openlibrary.org/authors/OL26320A.json"))
				.thenReturn("{\"bio\":{\"value\":\"British writer\"}}");

		when(spanishTranslation.translate("British writer")).thenReturn("Escritor británico");
		String[] result = useCase.findAuthor("J. R. R. Tolkien");

		assertThat(result).containsExactly(
				"Escritor británico",
				"https://covers.openlibrary.org/a/olid/OL26320A-L.jpg?default=false",
				ProviderEnum.OPEN_LIBRARY.name());
	}
}
