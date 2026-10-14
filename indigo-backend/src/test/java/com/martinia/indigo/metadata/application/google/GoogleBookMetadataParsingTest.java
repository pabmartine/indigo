package com.martinia.indigo.metadata.application.google;

import com.martinia.indigo.common.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleBookMetadataParsingTest {
    private final DataUtils data = mock(DataUtils.class);
    private final FindGoogleBooksBookUseCaseImpl useCase = new FindGoogleBooksBookUseCaseImpl();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(useCase, "dataUtils", data);
        ReflectionTestUtils.setField(useCase, "endpoint", "https://www.googleapis.com/books/v1/volumes?q=intitle:$title+inauthor:$author");
    }
    @Test void malformedCandidatesDoNotHideAValidOne() {
        when(data.getData(anyString())).thenReturn("""
            {"items":[{}, {"volumeInfo":{"title":null}},
             {"volumeInfo":{"title":"Dune","authors":["Frank Herbert"],"averageRating":"NaN"}},
             {"volumeInfo":{"title":"Dune","authors":["Frank Herbert"],"averageRating":4.5,"ratingsCount":"broken"}}]}
            """);
        var result = useCase.findBook("Dune", List.of("Frank Herbert"));
        assertThat(result.getRatingAverage()).isEqualTo(4.5F);
        assertThat(result.getRatingsCount()).isNull();
    }
    @Test void sequelIsNotAnEditionOfTheFirstBook() {
        when(data.getData(anyString())).thenReturn("""
            {"items":[{"volumeInfo":{"title":"Dune Messiah","authors":["Frank Herbert"],"averageRating":4.5}}]}
            """);
        assertThat(useCase.findBook("Dune", List.of("Frank Herbert"))).isNull();
    }
    @Test void editionAnnotationAndExplicitSubtitleAreSupported() {
        when(data.getData(anyString())).thenReturn("""
            {"items":[{"volumeInfo":{"title":"Dune (Book 1)","authors":["Frank Herbert"],"averageRating":4.5}}]}
            """);
        assertThat(useCase.findBook("Dune", List.of("Frank Herbert"))).isNotNull();
        when(data.getData(anyString())).thenReturn("""
            {"items":[{"volumeInfo":{"title":"Book","subtitle":"A story","averageRating":4.5}}]}
            """);
        assertThat(useCase.findBook("Book: A story", List.of())).isNotNull();
    }
    @Test void missingTitleDoesNotEmitAnHttpRequest() {
        assertThat(useCase.findBook(null, List.of())).isNull();
        verifyNoInteractions(data);
    }
    @Test void reservedCharactersAndAccentsAreEncodedWithoutChangingTheTitle() {
        when(data.getData(anyString())).thenReturn("{\"totalItems\":0}");
        useCase.findBook("¿Qué? A&B", List.of("Autor ñ"));
        verify(data).getData(contains("%C2%BFQu%C3%A9%3F+A%26B+inauthor:Autor+%C3%B1"));
    }
    @Test void errorPayloadIsNotReportedAsNoMatch() {
        when(data.getData(anyString())).thenReturn("{\"error\":{\"message\":\"quota\"}}");
        assertThatThrownBy(() -> useCase.findBook("Dune", List.of())).isInstanceOf(IllegalStateException.class);
    }
}
