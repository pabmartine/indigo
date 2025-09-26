package com.martinia.indigo.metadata.application.goodreads;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.common.domain.model.Review;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.DetectLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindGoodReadsReviewsUseCaseImplTest {

    @Mock
    private WebClient webClient;
    @Mock
    private DetectLibreTranslatePort detectLibreTranslatePort;
    @Mock
    private TranslateLibreTranslatePort translateLibreTranslatePort;

    private FindGoodReadsReviewsUseCaseImpl findGoodReadsReviewsUseCase;

    @BeforeEach
    void setUp() {
        findGoodReadsReviewsUseCase = new FindGoodReadsReviewsUseCaseImpl();
        ReflectionTestUtils.setField(findGoodReadsReviewsUseCase, "endpoint", "https://www.goodreads.com/search?q=$title+$author");
        ReflectionTestUtils.setField(findGoodReadsReviewsUseCase, "webClient", webClient);
        ReflectionTestUtils.setField(findGoodReadsReviewsUseCase, "detectLibreTranslatePort", Optional.of(detectLibreTranslatePort));
        ReflectionTestUtils.setField(findGoodReadsReviewsUseCase, "translateLibreTranslatePort", Optional.of(translateLibreTranslatePort));
    }

    @Test
    void getReviews_ShouldReturnReviews_WhenGoodReadsReturnsValidHtml() throws Exception {
        // Given
        String lang = "en";
        String title = "Test Book";
        List<String> authors = Collections.singletonList("Test Author");

        // Mocking the web client to return local HTML files
        WebClient realWebClient = new WebClient();
        File searchFile = new File("src/test/resources/goodreads/goodreads_search.html");
        HtmlPage searchPage = realWebClient.getPage(searchFile.toURI().toURL());
        when(webClient.getPage("https://www.goodreads.com/search?q=test+book+test+author")).thenReturn(searchPage);

        File reviewsFile = new File("src/test/resources/goodreads/goodreads_reviews.html");
        HtmlPage reviewsPage = realWebClient.getPage(reviewsFile.toURI().toURL());
        when(webClient.getPage("https://www.goodreads.com/book/show/12345.Test_Book")).thenReturn(reviewsPage);

        when(detectLibreTranslatePort.detect(any())).thenReturn("en");

        // When
        List<Review> reviews = findGoodReadsReviewsUseCase.getReviews(lang, title, authors);

        // Then
        assertNotNull(reviews);
        assertEquals(2, reviews.size());

        Review review1 = reviews.get(0);
        assertEquals("Test User", review1.getName());
        assertEquals(4, review1.getRating());
        assertEquals("This is a test review.", review1.getComment());
        assertNotNull(review1.getDate());

        Review review2 = reviews.get(1);
        assertEquals("Another User", review2.getName());
        assertEquals(2, review2.getRating());
        assertEquals("This is another test review.", review2.getComment());
        assertNotNull(review2.getDate());
    }
}
