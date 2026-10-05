package com.martinia.indigo.common.util;

import com.martinia.indigo.metadata.application.ProviderDiagnostics;
import com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientResponseException;

import java.net.InetSocketAddress;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class WikipediaHttpRequestsTest {
    private HttpServer server;
    private URL url;
    private final WikipediaHttpRequests requests = new WikipediaHttpRequests();
    private final AtomicInteger calls = new AtomicInteger();
    private final List<Instant> times = new ArrayList<>();

    @BeforeEach void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        url = new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        ReflectionTestUtils.setField(requests, "initialDelayMillis", 40L);
    }
    @AfterEach void tearDown() { server.stop(0); }

    private void responses(String retryAfter, int... statuses) {
        server.createContext("/", exchange -> {
            times.add(Instant.now());
            int status = statuses[Math.min(calls.getAndIncrement(), statuses.length - 1)];
            if (retryAfter != null && status >= 400) exchange.getResponseHeaders().add("Retry-After", retryAfter);
            byte[] body = "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @Test void retriesSameRequestWithIncreasingWaitAndResetsForNextRequest() throws Exception {
        responses(null, 429, 503, 200, 429, 200);
        ProviderDiagnostics.begin();
        try {
            assertThat(requests.getData(url, 0)).isEqualTo("{}");
            assertThat(requests.getData(url, 0)).isEqualTo("{}");
            assertThat(calls.get()).isEqualTo(5);
            assertThat(java.time.Duration.between(times.get(0), times.get(1)).toMillis()).isGreaterThanOrEqualTo(35);
            assertThat(java.time.Duration.between(times.get(1), times.get(2)).toMillis()).isGreaterThanOrEqualTo(75);
            assertThat(ProviderDiagnostics.events()).extracting(e -> e.getString("message"))
                    .containsExactly("Reintento 2 de 3 en 1 segundos", "Reintento 3 de 3 en 1 segundos", "Reintento 2 de 3 en 1 segundos");
        } finally { assertThat(ProviderDiagnostics.finish()).isEmpty(); }
    }

    @Test void transientFailuresAreBoundedAndDoNotOpenAFifteenMinuteCircuit() throws Exception {
        responses(null, 503, 503, 503, 200);
        assertThatThrownBy(() -> requests.getData(url, 0)).isInstanceOf(RestClientResponseException.class);
        assertThat(calls.get()).isEqualTo(3);
        assertThat(requests.getData(url, 0)).isEqualTo("{}");
    }

    @Test void permanentErrorsAndMissingPagesDoNotRetryOrBlockNextAuthor() throws Exception {
        responses(null, 403, 400, 404, 200);
        assertThatThrownBy(() -> requests.getData(url, 0)).isInstanceOf(RestClientResponseException.class);
        assertThat(calls.get()).isEqualTo(1);
        assertThatThrownBy(() -> requests.getData(url, 0)).isInstanceOf(RestClientResponseException.class);
        assertThat(calls.get()).isEqualTo(2);
        assertThat(requests.getData(url, 0)).isNull();
        assertThat(requests.getData(url, 0)).isEqualTo("{}");
    }

    @Test void honorsShortRetryAfterBeforeRetrying() throws Exception {
        responses("1", 429, 200);
        assertThat(requests.getData(url, 0)).isEqualTo("{}");
        assertThat(java.time.Duration.between(times.get(0), times.get(1)).toMillis()).isGreaterThanOrEqualTo(990);
    }

    @Test void longServerPauseIsRespectedWithoutBlockingTheJobOrSendingMoreRequests() {
        responses("120", 429);
        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> requests.getData(url, 0))
                    .isInstanceOfSatisfying(AccessRestrictedException.class, e ->
                            assertThat(e.retryAt()).isAfter(Instant.now().plusSeconds(110)));
        }
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test void fixedSpacingDoesNotGrowAfterRateLimits() throws Exception {
        responses(null, 429, 200, 200);
        requests.getData(url, 120);
        requests.getData(url, 120);
        assertThat(java.time.Duration.between(times.get(1), times.get(2)).toMillis()).isBetween(100L, 1000L);
    }

    @Test void cancellationDuringRetryStopsImmediatelyAndScopeIsRestored() throws Exception {
        AtomicBoolean active = new AtomicBoolean(true);
        server.createContext("/", exchange -> {
            calls.incrementAndGet();
            active.set(false);
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();
        assertThatThrownBy(() -> WikipediaHttpRequests.whileActive(active::get, () -> {
            try { return requests.getData(url, 0); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
        })).isInstanceOf(CancellationException.class);
        assertThat(calls.get()).isEqualTo(1);
        assertThat(WikipediaHttpRequests.whileActive(() -> true, () -> "next")).isEqualTo("next");
    }

    @Test void dataUtilsRoutesWikipediaThroughBoundedRetries() {
        responses(null, 503, 503, 503);
        DataUtils data = new DataUtils() { @Override String providerKey(String host) { return "wikipedia.org"; } };
        ReflectionTestUtils.setField(data, "wikipediaRequests", requests);
        ReflectionTestUtils.setField(data, "wikipediaMinimumIntervalMillis", 0L);
        assertThatThrownBy(() -> data.getData(url.toString())).hasRootCauseInstanceOf(RestClientResponseException.class);
        assertThat(calls.get()).isEqualTo(3);
    }
}
