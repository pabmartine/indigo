package com.martinia.indigo.metadata.application.reviews;

import com.gargoylesoftware.htmlunit.*;
import java.io.IOException;
import java.util.Locale;
import java.util.function.Supplier;

/** At the transport boundary so redirects and additional page requests share the same budget. */
public class ThrottledReviewConnection implements WebConnection {
    private final WebConnection delegate;
    private final Supplier<ReviewProviderRequestPolicy> policy;
    public ThrottledReviewConnection(WebConnection delegate, Supplier<ReviewProviderRequestPolicy> policy) {
        this.delegate = delegate;
        this.policy = policy;
    }
    @Override public WebResponse getResponse(WebRequest request) throws IOException {
        String host = request.getUrl().getHost().toLowerCase(Locale.ROOT);
        if (host.equals("goodreads.com") || host.endsWith(".goodreads.com")) policy.get().throttle("goodreads");
        else if (host.matches("(?:.*\\.)?amazon\\.(?:es|com|co\\.uk|de|fr|it|ca|com\\.au|co\\.jp)")) policy.get().throttle("amazon");
        return delegate.getResponse(request);
    }
    @Override public void close() throws IOException { delegate.close(); }
}
