package com.martinia.indigo.metadata.application.amazon;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.common.domain.model.Review;
import com.martinia.indigo.metadata.application.reviews.ReviewProviderRequestPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FindAmazonReviewsUseCaseImplTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"es-ES", "en", ""})
    void appliesLanguagePolicyToReviewContent(String language) throws Exception {
        var translation = (com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation)
                ReflectionTestUtils.getField(findAmazonReviewsUseCase, "spanishTranslation");
        var detector = org.mockito.Mockito.mock(com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.DetectLibreTranslatePort.class);
        var translator = org.mockito.Mockito.mock(com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort.class);
        ReflectionTestUtils.setField(translation, "detector", java.util.Optional.of(detector));
        ReflectionTestUtils.setField(translation, "translator", java.util.Optional.of(translator));
        String body = "This is a test review.";
        if (language.isEmpty()) when(detector.detect(body)).thenReturn("en");
        if (!language.startsWith("es")) when(translator.translate(body, "es")).thenReturn("Reseña traducida");
        try (WebClient client = new WebClient()) {
            HtmlPage search = client.getPage(new File("src/test/resources/amazon/amazon_search.html").toURI().toURL());
            HtmlPage page = client.getPage(new File("src/test/resources/amazon/amazon_reviews.html").toURI().toURL());
            ((com.gargoylesoftware.htmlunit.html.HtmlDivision) page.getFirstByXPath("//div[contains(@class,'review')]"))
                    .setAttribute("lang", language);
            when(webClient.getPage("https://www.amazon.com/s?k=test+book+test+author")).thenReturn(search);
            when(webClient.getPage("https://www.amazon.com/dp/B0000TEST")).thenReturn(page);
            Review result = findAmazonReviewsUseCase.getReviews("Test Book", List.of("Test Author")).get(0);
            assertEquals("es", result.getLanguage());
            assertEquals(language.startsWith("es") ? body : "Reseña traducida", result.getComment());
            verify(detector, org.mockito.Mockito.times(language.isEmpty() ? 1 : 0)).detect(body);
            verify(translator, org.mockito.Mockito.times(language.startsWith("es") ? 0 : 1)).translate(body, "es");
        }
    }

    @Test
    void fallsBackToPublicProductReviewsOnlyAfter404() throws Exception {
        ReflectionTestUtils.setField(findAmazonReviewsUseCase, "endpointReviews", "https://www.amazon.com/product-reviews/$asin?pageNumber=$numPage");
        try (WebClient client = new WebClient()) {
            HtmlPage search = client.getPage(new File("src/test/resources/amazon/amazon_search.html").toURI().toURL());
            HtmlPage reviews = client.getPage(new File("src/test/resources/amazon/amazon_reviews.html").toURI().toURL());
            when(webClient.getPage("https://www.amazon.com/s?k=test+book+test+author")).thenReturn(search);
            var notFound = org.mockito.Mockito.mock(com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException.class);
            when(notFound.getStatusCode()).thenReturn(404);
            when(webClient.getPage("https://www.amazon.com/product-reviews/B0000TEST?pageNumber=1"))
                    .thenThrow(notFound);
            when(webClient.getPage("https://www.amazon.com/dp/B0000TEST")).thenReturn(reviews);
            assertEquals(1, findAmazonReviewsUseCase.getReviews("Test Book", List.of("Test Author")).size());
        }
    }

    @Test
    void parsesSpanishDatesRegardlessOfJvmLocale() {
        java.util.Locale previous = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            assertEquals(java.time.LocalDate.of(2025, 9, 25),
                    FindAmazonReviewsUseCaseImpl.parseReviewDate("25 septiembre 2025")
                            .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        } finally {
            java.util.Locale.setDefault(previous);
        }
    }

    @Test
    void rejectsAnUnrelatedAuthorWithTheSameTitle() throws Exception {
        try (WebClient client = new WebClient()) {
            HtmlPage page = client.loadHtmlCodeIntoCurrentWindow("<html><div data-component-type='s-search-result' data-asin='OTHER'><h2><span>Test Book</span></h2><span>Other Author</span></div></html>");
            when(webClient.getPage(org.mockito.ArgumentMatchers.anyString())).thenReturn(page);
            assertTrue(findAmazonReviewsUseCase.getReviews("Test Book", List.of("Test Author")).isEmpty());
            verify(webClient, org.mockito.Mockito.times(1)).getPage(org.mockito.ArgumentMatchers.anyString());
        }
    }

    @Mock
    private WebClient webClient;

    private FindAmazonReviewsUseCaseImpl findAmazonReviewsUseCase;

    @BeforeEach
    void setUp() {
        findAmazonReviewsUseCase = new FindAmazonReviewsUseCaseImpl();
        ReflectionTestUtils.setField(findAmazonReviewsUseCase, "endpointAsin", "https://www.amazon.com/s?k=$title+$author");
        ReflectionTestUtils.setField(findAmazonReviewsUseCase, "endpointReviews", "https://www.amazon.com/dp/$asin");
        ReflectionTestUtils.setField(findAmazonReviewsUseCase, "webClient", webClient);
        var translation = new com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation();
        ReflectionTestUtils.setField(translation, "mongo", org.mockito.Mockito.mock(org.springframework.data.mongodb.core.MongoTemplate.class));
        ReflectionTestUtils.setField(translation, "translator", java.util.Optional.empty());
        ReflectionTestUtils.setField(findAmazonReviewsUseCase, "spanishTranslation", translation);
		ReflectionTestUtils.setField(findAmazonReviewsUseCase, "reviewProviderRequestPolicy", new ReviewProviderRequestPolicy());
    }

    @Test
	void getReviews_ShouldReturnReviews_WhenAmazonReturnsValidHtml() throws Exception {
        // Given
        String title = "Test Book";
        List<String> authors = Collections.singletonList("Test Author");

        // Mocking the web client to return local HTML files
        WebClient realWebClient = new WebClient();
        File searchFile = new File("src/test/resources/amazon/amazon_search.html");
        HtmlPage searchPage = realWebClient.getPage(searchFile.toURI().toURL());
        when(webClient.getPage("https://www.amazon.com/s?k=test+book+test+author")).thenReturn(searchPage);

        File reviewsFile = new File("src/test/resources/amazon/amazon_reviews.html");
        HtmlPage reviewsPage = realWebClient.getPage(reviewsFile.toURI().toURL());
		when(webClient.getPage("https://www.amazon.com/dp/B0000TEST")).thenReturn(reviewsPage);

        // When
        List<Review> reviews = findAmazonReviewsUseCase.getReviews(title, authors);

        // Then
        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        Review review = reviews.get(0);
        assertEquals("Test User", review.getName());
        assertEquals(5, review.getRating());
		assertEquals("This is a test review.", review.getComment());
		verify(webClient, never()).close();
    }

    @Test
	void getReviews_ShouldReturnEmptyList_WhenExceptionOccurs() throws Exception {
        // Given
        String title = "Test Book";
        List<String> authors = Collections.singletonList("Test Author");

        when(webClient.getPage(org.mockito.ArgumentMatchers.anyString())).thenThrow(new RuntimeException("Network error"));

        // When
		assertThrows(IllegalStateException.class, () -> findAmazonReviewsUseCase.getReviews(title, authors));
    }

    @Test
	void getReviews_ShouldReturnEmptyList_WhenNoAsinFound() throws Exception {
        // Given
        String title = "Non-existent Book";
        List<String> authors = Collections.singletonList("Unknown Author");

        // Create empty HTML page
        WebClient realWebClient = new WebClient();
        String emptyHtml = "<html><body><div>No results found</div></body></html>";
        HtmlPage emptyPage = realWebClient.loadHtmlCodeIntoCurrentWindow(emptyHtml);
        when(webClient.getPage(org.mockito.ArgumentMatchers.anyString())).thenReturn(emptyPage);

        // When
        List<Review> reviews = findAmazonReviewsUseCase.getReviews(title, authors);

        // Then
		assertTrue(reviews.isEmpty());
    }

    @Test
    void getReviews_ShouldHandleEmptyAuthorsList() throws Exception {
        // Given
        String title = "Test Book";
        List<String> authors = Collections.emptyList();

        // When
        List<Review> reviews = findAmazonReviewsUseCase.getReviews(title, authors);

        // Then
		assertTrue(reviews.isEmpty());
    }

    @Test
    void getReviews_ShouldHandleNullTitle() throws Exception {
        // Given
        String title = null;
        List<String> authors = Collections.singletonList("Test Author");

        // When
        List<Review> reviews = findAmazonReviewsUseCase.getReviews(title, authors);

        // Then
		assertTrue(reviews.isEmpty());
    }

    @Test
    void getReviews_ShouldHandleNullAuthors() throws Exception {
        // Given
        String title = "Test Book";
        List<String> authors = null;

        // When
        List<Review> reviews = findAmazonReviewsUseCase.getReviews(title, authors);

        // Then
		assertTrue(reviews.isEmpty());
    }
}
