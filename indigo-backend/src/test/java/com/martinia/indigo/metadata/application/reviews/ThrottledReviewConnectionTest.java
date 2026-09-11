package com.martinia.indigo.metadata.application.reviews;

import com.gargoylesoftware.htmlunit.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class ThrottledReviewConnectionTest {
    @Test void eachTransportRequestIncludingRedirectTargetsUsesProviderBudget() throws Exception {
        var delegate = mock(WebConnection.class); var policy = mock(ReviewProviderRequestPolicy.class);
        var connection = new ThrottledReviewConnection(delegate, () -> policy);
        for (String url : java.util.List.of("https://www.amazon.es/search", "https://www.amazon.es/dp/book", "https://www.amazon.es/redirect", "https://www.goodreads.com/search"))
            connection.getResponse(new WebRequest(java.net.URI.create(url).toURL()));
        verify(policy, times(3)).throttle("amazon"); verify(policy).throttle("goodreads");
    }
    @Test void pausedRequestDoesNotReachNetwork() throws Exception {
        var delegate = mock(WebConnection.class); var policy = mock(ReviewProviderRequestPolicy.class);
        doThrow(new java.util.concurrent.CancellationException()).when(policy).throttle("amazon");
        var connection = new ThrottledReviewConnection(delegate, () -> policy);
        org.junit.jupiter.api.Assertions.assertThrows(java.util.concurrent.CancellationException.class,
                () -> connection.getResponse(new WebRequest(java.net.URI.create("https://amazon.es/dp/book").toURL())));
        verifyNoInteractions(delegate);
    }
}
