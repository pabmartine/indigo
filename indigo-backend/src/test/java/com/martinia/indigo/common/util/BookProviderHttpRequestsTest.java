package com.martinia.indigo.common.util;

import com.martinia.indigo.metadata.application.ProviderDiagnostics;
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

class BookProviderHttpRequestsTest {
    private HttpServer server;
    private URL url;
    private final BookProviderHttpRequests requests = new BookProviderHttpRequests();
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
        responses(null, 503, 503, 200, 503, 200);
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

    @Test void serverPauseLongerThanLocalBudgetRetainsRequestAndDoesNotRecordAnError() throws Exception {
        ReflectionTestUtils.setField(requests, "maxWaitMillis", 150L);
        responses("1", 429, 200);
        var waits = new ArrayList<Instant>();
        ProviderDiagnostics.begin();
        try {
            assertThat(BookProviderHttpRequests.whileActive(() -> true, waits::add, () -> {
                try { return requests.getData(url, 0); }
                catch (java.io.IOException e) { throw new IllegalStateException(e); }
            })).isEqualTo("{}");
            assertThat(calls.get()).isEqualTo(2);
            assertThat(java.time.Duration.between(times.get(0), times.get(1)).toMillis()).isGreaterThanOrEqualTo(990);
            assertThat(waits).hasSize(2);
            assertThat(waits.get(0)).isNotNull();
            assertThat(waits.get(1)).isNull();
            assertThat(ProviderDiagnostics.events()).allSatisfy(event -> assertThat(event.getString("status")).isEqualTo("WAITING"));
        } finally { assertThat(ProviderDiagnostics.finish()).isEmpty(); }
    }

    @Test void repeatedServerPausesStillUseOnlyThreeAttemptsAndNextRequestWaitsForTheLastPause() throws Exception {
        ReflectionTestUtils.setField(requests, "maxWaitMillis", 150L);
        responses("1", 429, 429, 429, 200);
        assertThatThrownBy(() -> requests.getData(url, 0)).isInstanceOf(RestClientResponseException.class);
        assertThat(calls.get()).isEqualTo(3);
        assertThat(requests.getData(url, 0)).isEqualTo("{}");
        assertThat(calls.get()).isEqualTo(4);
        for (int i = 1; i < times.size(); i++) {
            assertThat(java.time.Duration.between(times.get(i - 1), times.get(i)).toMillis()).isGreaterThanOrEqualTo(990);
        }
    }

    @Test void longProviderWaitCanBeCancelledAndClearsTheVisibleWaitState() {
        responses("120", 429);
        var active = new AtomicBoolean(true);
        var waits = new ArrayList<Instant>();
        assertThatThrownBy(() -> BookProviderHttpRequests.whileActive(active::get, until -> {
            waits.add(until);
            if (until != null) active.set(false);
        }, () -> {
            try { return requests.getData(url, 0); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
        })).isInstanceOf(CancellationException.class);
        assertThat(calls.get()).isEqualTo(1);
        assertThat(waits).hasSize(2);
        assertThat(waits.get(0)).isAfter(Instant.now().plusSeconds(110));
        assertThat(waits.get(1)).isNull();
    }

    @Test void fixedSpacingDoesNotGrowAfterRateLimits() throws Exception {
        responses(null, 503, 200, 200);
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
        assertThatThrownBy(() -> BookProviderHttpRequests.whileActive(active::get, () -> {
            try { return requests.getData(url, 0); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
        })).isInstanceOf(CancellationException.class);
        assertThat(calls.get()).isEqualTo(1);
        assertThat(BookProviderHttpRequests.whileActive(() -> true, () -> "next")).isEqualTo("next");
    }

    @Test void dataUtilsRoutesBookHostsToThisClient() throws Exception {
        var client = org.mockito.Mockito.mock(BookProviderHttpRequests.class);
        var data = new DataUtils();
        ReflectionTestUtils.setField(data, "bookRequests", client);
        org.mockito.Mockito.when(client.getData(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyLong())).thenReturn("{}");
        assertThat(data.getData("https://www.googleapis.com/books/v1/volumes?q=test")).isEqualTo("{}");
        assertThat(data.getData("https://openlibrary.org/search.json?q=test")).isEqualTo("{}");
        org.mockito.Mockito.verify(client).getData(new URL("https://www.googleapis.com/books/v1/volumes?q=test"), 1000L);
        org.mockito.Mockito.verify(client).getData(new URL("https://openlibrary.org/search.json?q=test"), 500L);
    }
    @Test void rateLimitWithoutHeaderWaitsOnTheSameBookAndCanBeCancelled() {
        responses(null, 429);
        var active = new AtomicBoolean(true);
        var waits = new ArrayList<Instant>();
        assertThatThrownBy(() -> BookProviderHttpRequests.whileActive(active::get, until -> {
            waits.add(until);
            if (until != null) active.set(false);
        }, () -> {
            try { return requests.getData(url, 0); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
        })).isInstanceOf(CancellationException.class);
        assertThat(calls.get()).isEqualTo(1);
        assertThat(waits.get(0)).isAfter(Instant.now().plusSeconds(50));
        assertThat(waits.get(1)).isNull();
    }
    @Test void clientKeepsProvidersIndependent() throws Exception {
        responses("1", 429, 200);
        ReflectionTestUtils.setField(requests, "attempts", 1);
        assertThatThrownBy(() -> requests.getData(url, 0)).isInstanceOf(RestClientResponseException.class);
        // Same server via another host has a separate lock/pause.
        URL other = new URL(url.toString().replace("127.0.0.1", "localhost"));
        var waits = new ArrayList<Instant>();
        assertThat(BookProviderHttpRequests.whileActive(() -> true, waits::add, () -> {
            try { return requests.getData(other, 0); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
        })).isEqualTo("{}");
        assertThat(waits).isEmpty();
    }
}
