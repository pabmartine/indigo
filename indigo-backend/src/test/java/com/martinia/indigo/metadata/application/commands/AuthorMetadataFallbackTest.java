package com.martinia.indigo.metadata.application.commands;

import java.util.Date;
import java.util.Optional;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.metadata.application.openlibrary.AuthorCatalogTranslationException;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorCatalogPort;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorPort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthorMetadataFallbackTest {

	private final AuthorRepository repository = mock(AuthorRepository.class);
	private final FindOpenLibraryAuthorCatalogPort catalog = mock(FindOpenLibraryAuthorCatalogPort.class);
	private final FindWikipediaAuthorPort wikipedia = mock(FindWikipediaAuthorPort.class);
	private final FindOpenLibraryAuthorPort openLibrary = mock(FindOpenLibraryAuthorPort.class);
	private final ImageUtils images = mock(ImageUtils.class);
	private final FindAuthorMetadataUseCaseImpl useCase = new FindAuthorMetadataUseCaseImpl();
	private final AuthorMongoEntity author = AuthorMongoEntity.builder().id("author").name("Author").build();

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(useCase, "authorRepository", repository);
		ReflectionTestUtils.setField(useCase, "findOpenLibraryAuthorCatalogPort", Optional.of(catalog));
		ReflectionTestUtils.setField(useCase, "findWikipediaAuthorPort", Optional.of(wikipedia));
		ReflectionTestUtils.setField(useCase, "findOpenLibraryAuthorPort", Optional.of(openLibrary));
		ReflectionTestUtils.setField(useCase, "imageUtils", images);
		when(repository.findById("author")).thenReturn(Optional.of(author));
		when(images.getBase64AuthorUrl(anyString())).thenAnswer(invocation -> "base64:" + invocation.getArgument(0));
	}

	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void fillsWikipediaBiographyWhileKeepingTheCatalogPhoto(final boolean override) {
		if (override) {
			author.setDescription("Descripción anterior");
			author.setImage("previous-photo");
		}
		when(catalog.findAuthor("Author")).thenReturn(new String[] { null, "catalog-photo", "OPEN_LIBRARY" });
		when(wikipedia.findAuthor("Author", "es", 0)).thenReturn(new String[] { "Biografía", "wiki-photo", "WIKIPEDIA" });

		assertThat(useCase.find("author", override, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		assertThat(author.getDescription()).isEqualTo("Biografía");
		assertThat(author.getImage()).isEqualTo("base64:catalog-photo");
		assertThat(author.getMetadataSources()).containsEntry("description", "WIKIPEDIA").containsEntry("image", "OPEN_LIBRARY");
		verifyNoInteractions(openLibrary);
		verify(wikipedia, never()).findAuthor("Author", "en", 0);
	}

	@Test
	void keepsCatalogBiographyWhenWikipediaSuppliesTheMissingPhotoInOverrideMode() {
		when(catalog.findAuthor("Author")).thenReturn(new String[] { "Del índice", null, "OPEN_LIBRARY" });
		when(wikipedia.findAuthor("Author", "es", 0)).thenReturn(new String[] { "De Wikipedia", "wiki-photo", "WIKIPEDIA" });

		useCase.find("author", true, 0, "es");
		assertThat(author.getDescription()).isEqualTo("Del índice");
		assertThat(author.getImage()).isEqualTo("base64:wiki-photo");
	}

	@Test
	void triesEnglishWikipediaWhenTheFirstResultContainsOnlyAPhoto() {
		when(wikipedia.findAuthor("Author", "es", 0)).thenReturn(new String[] { " ", "es-photo", "WIKIPEDIA" });
		when(wikipedia.findAuthor("Author", "en", 0)).thenReturn(new String[] { "Biography", "en-photo", "WIKIPEDIA" });

		useCase.find("author", true, 0, "es");
		assertThat(author.getDescription()).isEqualTo("Biography");
		assertThat(author.getImage()).isEqualTo("base64:es-photo");
		verifyNoInteractions(openLibrary);
	}

	@Test
	void fallsBackToOpenLibraryHttpForBiographyEvenWithACatalogPhoto() {
		when(catalog.findAuthor("Author")).thenReturn(new String[] { null, "catalog-photo", "OPEN_LIBRARY" });
		when(openLibrary.findAuthor("Author")).thenReturn(new String[] { "Biografía HTTP", "http-photo", "OPEN_LIBRARY" });

		useCase.find("author", true, 0, "es");
		assertThat(author.getDescription()).isEqualTo("Biografía HTTP");
		assertThat(author.getImage()).isEqualTo("base64:catalog-photo");
		verify(wikipedia).findAuthor("Author", "es", 0);
		verify(wikipedia).findAuthor("Author", "en", 0);
	}

	@Test
	void continuesToWikipediaIfTheCatalogBiographyCannotBeTranslated() {
		when(catalog.findAuthor("Author")).thenThrow(new AuthorCatalogTranslationException("Unavailable"));
		when(wikipedia.findAuthor("Author", "es", 0)).thenReturn(new String[] { "Biografía", "wiki-photo", "WIKIPEDIA" });

		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		assertThat(author.getDescription()).isEqualTo("Biografía");
		assertThat(author.getLastMetadataSync()).isNull();
	}

	@Test
	void retriesMissingBiographyDespiteARecentImageOnlySync() {
		author.setImage("existing-photo");
		author.setProvider("OPEN_LIBRARY");
		author.setDescription(" ");
		author.setLastMetadataSync(new Date());
		when(wikipedia.findAuthor("Author", "es", 0)).thenReturn(new String[] { "Biografía", "wiki-photo", "WIKIPEDIA" });

		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		assertThat(author.getDescription()).isEqualTo("Biografía");
		assertThat(author.getImage()).isEqualTo("existing-photo");
	}

	@Test
	void imageOnlyMatchesDoNotCountAsFoundWhenThePhotoAlreadyExists() {
		author.setImage("existing-photo");
		author.setProvider("OPEN_LIBRARY");
		author.setLastMetadataSync(new Date());
		when(catalog.findAuthor("Author")).thenReturn(new String[] { null, "catalog-photo", "OPEN_LIBRARY" });

		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.NOT_FOUND);
		assertThat(author.getDescription()).isNull();
		assertThat(author.getImage()).isEqualTo("existing-photo");
		verify(wikipedia).findAuthor("Author", "es", 0);
		verify(wikipedia).findAuthor("Author", "en", 0);
		verify(openLibrary).findAuthor("Author");
		verifyNoInteractions(images);
	}

	@Test
	void retriesMissingPhotoDespiteARecentSyncAndKeepsBiography() {
		author.setDescription("Existing biography");
		author.setProvider("OPEN_LIBRARY");
		author.setLastMetadataSync(new Date());
		when(catalog.findAuthor("Author")).thenReturn(new String[] { "Other biography", "catalog-photo", "OPEN_LIBRARY" });

		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		assertThat(author.getDescription()).isEqualTo("Existing biography");
		assertThat(author.getImage()).isEqualTo("base64:catalog-photo");
	}

	@Test
	void skipsCompleteAuthorsEvenWhenTheLegacyProviderIsMissing() {
		author.setDescription("Existing biography");
		author.setImage("existing-photo");

		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.SKIPPED);
		verifyNoInteractions(catalog, wikipedia, openLibrary, images);
	}

	@Test
	void avoidsFallbackWhenTheCatalogHasBothFields() {
		when(catalog.findAuthor("Author")).thenReturn(new String[] { "Biografía", "catalog-photo", "OPEN_LIBRARY" });

		useCase.find("author", false, 0, "es");
		verifyNoInteractions(wikipedia, openLibrary);
	}

	@Test
	void toleratesAnIncompleteProviderResponseAndKeepsSearching() {
		when(catalog.findAuthor("Author")).thenReturn(new String[] {});
		when(wikipedia.findAuthor("Author", "es", 0)).thenReturn(new String[] { "Biografía", "wiki-photo", "WIKIPEDIA" });

		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		assertThat(author.getDescription()).isEqualTo("Biografía");
	}

	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void triesAnotherPhotoWhenTheCatalogUrlCannotBeDownloaded(boolean override) {
		when(catalog.findAuthor("Author")).thenReturn(new String[] { "Del índice", "broken-photo", "OPEN_LIBRARY" });
		when(images.getBase64AuthorUrl("broken-photo")).thenReturn(null);
		when(wikipedia.findAuthor("Author", "es", 0)).thenReturn(new String[] { "Otra biografía", "wiki-photo", "WIKIPEDIA" });
		assertThat(useCase.find("author", override, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		assertThat(author.getDescription()).isEqualTo("Del índice");
		assertThat(author.getImage()).isEqualTo("base64:wiki-photo");
		verifyNoInteractions(openLibrary);
	}

	@Test
	void retainsTheCatalogPhotoWhenTranslationFailsAndWikipediaCompletesTheBiography() {
		when(catalog.findAuthor("Author")).thenThrow(new AuthorCatalogTranslationException("Unavailable", "catalog-photo", null));
		when(wikipedia.findAuthor("Author", "es", 0)).thenReturn(new String[] { "Biografía", "wiki-photo", "WIKIPEDIA" });
		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		assertThat(author.getImage()).isEqualTo("base64:catalog-photo");
		assertThat(author.getMetadataSources()).containsEntry("description", "WIKIPEDIA").containsEntry("image", "OPEN_LIBRARY");
	}

	@Test
	void savesPartialProgressButRemainsRetryableIfAPhotoProviderFails() {
		when(catalog.findAuthor("Author")).thenReturn(new String[] { "Biografía", null, "OPEN_LIBRARY" });
		when(wikipedia.findAuthor("Author", "es", 0)).thenThrow(new IllegalStateException("Temporarily unavailable"));
		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.ERROR);
		assertThat(author.getDescription()).isEqualTo("Biografía");
		assertThat(author.getLastMetadataSync()).isNull();
		verify(repository).save(author);
		verify(wikipedia).findAuthor("Author", "en", 0);
		verify(openLibrary).findAuthor("Author");
	}

	@Test
	void existingBiographyAvoidsUnnecessaryWikipediaRequestsWhenCatalogFillsThePhoto() {
		author.setDescription("Biografía existente");
		when(catalog.findAuthor("Author")).thenReturn(new String[] { null, "catalog-photo", "OPEN_LIBRARY" });
		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		verifyNoInteractions(wikipedia, openLibrary);
	}

	@Test
	void imageTimeoutDoesNotDiscardTheBiographyAndFallsBackToAnotherProvider() {
		when(catalog.findAuthor("Author")).thenReturn(new String[] { "Biografía", "timeout-photo", "OPEN_LIBRARY" });
		when(images.getBase64AuthorUrl("timeout-photo")).thenThrow(new IllegalStateException("Timeout"));
		when(openLibrary.findAuthor("Author")).thenReturn(new String[] { null, "http-photo", "OPEN_LIBRARY" });
		assertThat(useCase.find("author", false, 0, "es")).isEqualTo(MetadataItemResult.FOUND);
		assertThat(author.getDescription()).isEqualTo("Biografía");
		assertThat(author.getImage()).isEqualTo("base64:http-photo");
	}

	@Test
	void cancellationDoesNotContinueToOtherProvidersOrSaveAnInspection() {
		when(catalog.findAuthor("Author")).thenThrow(new java.util.concurrent.CancellationException());
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> useCase.find("author", false, 0, "es"))
				.isInstanceOf(java.util.concurrent.CancellationException.class);
		verifyNoInteractions(wikipedia, openLibrary);
		verify(repository, never()).save(any());
	}
	@Test
	void pausedWikipediaSkipsOtherLanguagesButStillTriesOpenLibraryAndKeepsAuthorPending() {
		when(wikipedia.findAuthor("Author", "es", 0)).thenThrow(new IllegalStateException(
				new com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException(
						"Paused", java.time.Instant.now().plusSeconds(60))));
		assertThat(useCase.find("author", false, 0, "en")).isEqualTo(MetadataItemResult.ERROR);
		verify(wikipedia).findAuthor("Author", "es", 0);
		verify(wikipedia, never()).findAuthor("Author", "en", 0);
		verify(openLibrary).findAuthor("Author");
	}

}
