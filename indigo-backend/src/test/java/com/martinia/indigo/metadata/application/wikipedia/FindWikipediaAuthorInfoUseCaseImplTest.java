package com.martinia.indigo.metadata.application.wikipedia;

import com.martinia.indigo.common.util.DataUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

class FindWikipediaAuthorInfoUseCaseImplTest {
    @Test void translatesEnglishBiographyAndPreservesThePhotoIfTranslationFails() {
        DataUtils data = mock(DataUtils.class);
        var translator = mock(com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort.class);
        var useCase = new FindWikipediaAuthorInfoUseCaseImpl();
        ReflectionTestUtils.setField(useCase, "dataUtils", data);
        ReflectionTestUtils.setField(useCase, "translateLibreTranslatePort", Optional.of(translator));
        ReflectionTestUtils.setField(useCase, "endpoint", "https://$lang.wikipedia.org/$subject");
        when(data.getData("https://en.wikipedia.org/Author")).thenReturn("""
                {"query":{"pages":{"1":{"title":"Author","extract":"Biography","original":{"source":"photo"}}}}}
                """);
        when(translator.translate("Biography", "es")).thenReturn("Biografía", null);
        assertArrayEquals(new String[]{"Biografía", "photo", "WIKIPEDIA"}, useCase.getAuthorInfo("Author", "en"));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> useCase.getAuthorInfo("Author", "en"))
                .isInstanceOfSatisfying(com.martinia.indigo.metadata.application.AuthorMetadataTranslationException.class,
                        failure -> assertArrayEquals(new String[]{null, "photo", "WIKIPEDIA"}, failure.getPartialMetadata()));
    }

    @Test
    void preservesExactTitleIncludingAccentsAndDisambiguationWithoutRequiringAnImage() {
        DataUtils data = mock(DataUtils.class);
        var useCase = new FindWikipediaAuthorInfoUseCaseImpl();
        ReflectionTestUtils.setField(useCase, "dataUtils", data);
        ReflectionTestUtils.setField(useCase, "translateLibreTranslatePort", Optional.empty());
        ReflectionTestUtils.setField(useCase, "endpoint", "https://$lang.wikipedia.org/w/api.php?titles=$subject");
        when(data.getData("https://es.wikipedia.org/w/api.php?titles=Mart%C3%AD%20Olivella%20%28escritor%29"))
                .thenReturn("""
                        {"query":{"pages":{"1":{"title":"Martí Olivella (escritor)","extract":"Biografía"}}}}
                        """);
        assertArrayEquals(new String[]{"Biografía", null, "WIKIPEDIA"},
                useCase.getAuthorInfo("Martí Olivella (escritor)", "es"));
    }
}
