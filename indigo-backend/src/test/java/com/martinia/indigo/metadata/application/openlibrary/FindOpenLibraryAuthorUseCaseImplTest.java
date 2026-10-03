package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.application.AuthorMetadataTranslationException;
import com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindOpenLibraryAuthorUseCaseImplTest {
    private final DataUtils data = mock(DataUtils.class);
    private final CachedSpanishTranslation translation = mock(CachedSpanishTranslation.class);
    private final FindOpenLibraryAuthorUseCaseImpl useCase = new FindOpenLibraryAuthorUseCaseImpl();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(useCase, "dataUtils", data);
        ReflectionTestUtils.setField(useCase, "spanishTranslation", translation);
        ReflectionTestUtils.setField(useCase, "authorsEndpoint", "https://openlibrary.org/search/authors.json?q=$subject");
        ReflectionTestUtils.setField(useCase, "authorInfoEndpoint", "https://openlibrary.org/authors/$id.json");
        ReflectionTestUtils.setField(useCase, "authorImageEndpoint", "https://covers.openlibrary.org/a/olid/$id-L.jpg?default=false");
        when(data.getData("https://openlibrary.org/search/authors.json?q=Author"))
                .thenReturn("{\"docs\":[{\"name\":\"Author\",\"key\":\"OL1A\"}]}");
    }

    @Test void returnsPhotoWithoutRequiringBiography() {
        when(data.getData("https://openlibrary.org/authors/OL1A.json")).thenReturn("{\"photos\":[123]}");
        assertThat(useCase.findAuthor("Author")).containsExactly(null,
                "https://covers.openlibrary.org/a/olid/OL1A-L.jpg?default=false", "OPEN_LIBRARY");
        verifyNoInteractions(translation);
    }

    @Test void translatesBiographyWithoutInventingAPhoto() {
        when(data.getData("https://openlibrary.org/authors/OL1A.json"))
                .thenReturn("{\"bio\":{\"value\":\"English biography\"},\"photos\":[-1]}");
        when(translation.translate("English biography")).thenReturn("Biografía en español");
        assertThat(useCase.findAuthor("Author")).containsExactly("Biografía en español", null, "OPEN_LIBRARY");
    }

    @Test void preservesPhotoWhenTranslationThrows() {
        when(data.getData("https://openlibrary.org/authors/OL1A.json"))
                .thenReturn("{\"bio\":\"Biography\",\"photos\":[123]}");
        when(translation.translate("Biography")).thenThrow(new IllegalStateException("Unavailable"));
        assertThatThrownBy(() -> useCase.findAuthor("Author"))
                .isInstanceOfSatisfying(AuthorMetadataTranslationException.class, failure ->
                        assertThat(failure.getPartialMetadata()).containsExactly(null,
                                "https://covers.openlibrary.org/a/olid/OL1A-L.jpg?default=false", "OPEN_LIBRARY"));
    }
}
