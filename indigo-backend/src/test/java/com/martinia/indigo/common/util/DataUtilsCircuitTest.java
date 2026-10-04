package com.martinia.indigo.common.util;

import com.sun.net.httpserver.HttpServer;
import com.martinia.indigo.metadata.application.ProviderDiagnostics;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;

class DataUtilsCircuitTest {
    @Test
    void wikipediaLearnsIntervalAndWaitsBeforeRetryingWithoutCountingBlockedRequests() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/", exchange -> {
            int attempt = requests.incrementAndGet();
            if (attempt == 2) {
                byte[] body = "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } else exchange.sendResponseHeaders(429, -1);
            exchange.close();
        });
        server.start();
        try {
            // Route the local test server through the shared Wikipedia policy.
            DataUtils data = new DataUtils() {
                @Override String providerKey(String host) { return "wikipedia.org"; }
            };
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            assertThatThrownBy(() -> data.getData(url)).isInstanceOf(IllegalStateException.class);
            var states = (java.util.Map<?, ?>) ReflectionTestUtils.getField(data, "providerStates");
            Object state = states.get("wikipedia.org");
            assertThat(ReflectionTestUtils.getField(state, "wikipediaIntervalMillis")).isEqualTo(2000L);
            assertThatThrownBy(() -> data.getData(url)).isInstanceOf(IllegalStateException.class);
            assertThat(requests.get()).isEqualTo(1);
            assertThat(ReflectionTestUtils.getField(state, "wikipediaIntervalMillis")).isEqualTo(2000L);
            ReflectionTestUtils.setField(state, "blockedUntil", java.time.Instant.EPOCH);
            long start = System.nanoTime();
            assertThat(data.getData(url)).isEqualTo("{}");
            assertThat(java.time.Duration.ofNanos(System.nanoTime() - start).toMillis()).isGreaterThanOrEqualTo(1800);
            assertThat(ReflectionTestUtils.getField(state, "wikipediaIntervalMillis")).isEqualTo(2000L);
            assertThatThrownBy(() -> data.getData(url)).isInstanceOf(IllegalStateException.class);
            assertThat(ReflectionTestUtils.getField(state, "wikipediaIntervalMillis")).isEqualTo(3000L);
            assertThat(requests.get()).isEqualTo(3);
        } finally { server.stop(0); }
    }

    @Test
    void successfulRequestsBetweenRateLimitsDoNotResetTheBackoff() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/", exchange -> {
            int attempt = requests.incrementAndGet();
            if (attempt == 2) {
                byte[] body = "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } else exchange.sendResponseHeaders(429, -1);
            exchange.close();
        });
        server.start();
        try {
            DataUtils data = new DataUtils();
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            assertThatThrownBy(() -> data.getData(url)).isInstanceOf(IllegalStateException.class);
            var states = (java.util.Map<?, ?>) ReflectionTestUtils.getField(data, "providerStates");
            Object state = states.get("127.0.0.1");
            ReflectionTestUtils.setField(state, "blockedUntil", java.time.Instant.EPOCH);
            assertThat(data.getData(url)).isEqualTo("{}");
            java.time.Instant before = java.time.Instant.now();
            assertThatThrownBy(() -> data.getData(url)).satisfies(error -> {
                var paused = (com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException)
                        org.springframework.core.NestedExceptionUtils.getMostSpecificCause(error);
                assertThat(paused.retryAt()).isAfterOrEqualTo(before.plusSeconds(120));
            });
            ReflectionTestUtils.setField(state, "lastRateLimit", java.time.Instant.now().minusSeconds(1801));
            ReflectionTestUtils.invokeMethod(data, "resetRateLimitsAfterRecovery", state);
            assertThat(ReflectionTestUtils.getField(state, "rateLimitFailures")).isEqualTo(0);
        } finally { server.stop(0); }
    }

    @Test
    void repeatedRateLimitsUseIncreasingDelaysWithoutWaitingInTest() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(429, -1);
            exchange.close();
        });
        server.start();
        try {
            DataUtils data = new DataUtils();
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            for (long seconds : new long[]{60, 120, 240, 480, 900, 900}) {
                java.time.Instant before = java.time.Instant.now();
                assertThatThrownBy(() -> data.getData(url)).satisfies(error -> {
                    var paused = (com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException)
                            org.springframework.core.NestedExceptionUtils.getMostSpecificCause(error);
                    assertThat(paused.retryAt()).isBetween(before.plusSeconds(seconds), java.time.Instant.now().plusSeconds(seconds));
                });
                var states = (java.util.Map<?, ?>) ReflectionTestUtils.getField(data, "providerStates");
                ReflectionTestUtils.setField(states.get("127.0.0.1"), "blockedUntil", java.time.Instant.EPOCH);
            }
        } finally { server.stop(0); }
    }

    @Test
    void firstRateLimitResponsePausesAndHonorsRetryAfter() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            exchange.getResponseHeaders().add("Retry-After", "5");
            exchange.sendResponseHeaders(429, -1);
            exchange.close();
        });
        server.start();
        try {
            DataUtils data = new DataUtils();
            ReflectionTestUtils.setField(data, "initialDelaySeconds", 1L);
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            for (int i = 0; i < 2; i++) {
                assertThatThrownBy(() -> data.getData(url)).satisfies(error -> {
                    var cause = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(error);
                    assertThat(cause).isInstanceOf(com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException.class);
                    var paused = (com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException) cause;
                    assertThat(paused.retryAt()).isAfter(java.time.Instant.now().plusSeconds(3));
                });
            }
            assertThat(requests.get()).isEqualTo(1);
            assertThat(ReflectionTestUtils.invokeMethod(data, "providerKey", "es.wikipedia.org").toString()).isEqualTo("wikipedia.org");
            assertThat(ReflectionTestUtils.invokeMethod(data, "providerKey", "en.wikipedia.org").toString()).isEqualTo("wikipedia.org");
            assertThat(data.awaitWikipediaAvailable(() -> false)).isFalse();
        } finally { server.stop(0); }
    }

    @Test
    void shortRetryAfterCannotShortenProgressiveBackoff() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Retry-After", "1");
            exchange.sendResponseHeaders(429, -1);
            exchange.close();
        });
        server.start();
        try {
            DataUtils data = new DataUtils();
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            for (long seconds : new long[]{60, 120, 240}) {
                java.time.Instant before = java.time.Instant.now();
                assertThatThrownBy(() -> data.getData(url)).satisfies(error -> {
                    var paused = (com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException)
                            org.springframework.core.NestedExceptionUtils.getMostSpecificCause(error);
                    assertThat(paused.retryAt()).isBetween(before.plusSeconds(seconds), java.time.Instant.now().plusSeconds(seconds));
                });
                var states = (java.util.Map<?, ?>) ReflectionTestUtils.getField(data, "providerStates");
                ReflectionTestUtils.setField(states.get("127.0.0.1"), "blockedUntil", java.time.Instant.EPOCH);
            }
        } finally { server.stop(0); }
    }

    @Test
    void pausesAfterRepeatedFailuresWithoutSendingMoreRequestsAndReportsRetryTime() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();
        try {
            DataUtils data = new DataUtils();
            ReflectionTestUtils.setField(data, "circuitBreakerFailures", 3);
            ReflectionTestUtils.setField(data, "circuitBreakerCooldownMinutes", 15L);
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            for (int i = 0; i < 3; i++) {
                assertThatThrownBy(() -> data.getData(url)).hasRootCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);
            }
            ProviderDiagnostics.begin();
            try {
                try { data.getData(url); fail("Expected a paused provider"); }
                catch (IllegalStateException error) {
                    assertThat(org.springframework.core.NestedExceptionUtils.getMostSpecificCause(error))
                            .isInstanceOf(org.springframework.web.client.RestClientResponseException.class)
                            .hasMessageContaining("503");
                    ProviderDiagnostics.record("WIKIPEDIA", "Obtener autor", error);
                }
            } finally {
                var diagnostics = ProviderDiagnostics.finish();
                assertThat(diagnostics).hasSize(1);
                assertThat(diagnostics.get(0).getString("code")).isEqualTo("PAUSED");
                assertThat(diagnostics.get(0).getDate("retryAt")).isNotNull();
                assertThat(diagnostics.get(0).getInteger("httpStatus")).isEqualTo(503);
            }
            assertThat(requests.get()).isEqualTo(3);
        } finally { server.stop(0); }
    }
    @Test
    void missingPagesDoNotPauseProviderAndServerFailuresKeepHttpStatus() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/missing", exchange -> { exchange.sendResponseHeaders(404, -1); exchange.close(); });
        server.createContext("/failed", exchange -> { exchange.sendResponseHeaders(503, -1); exchange.close(); });
        server.start();
        try {
            DataUtils data = new DataUtils();
            ReflectionTestUtils.setField(data, "circuitBreakerFailures", 3);
            ReflectionTestUtils.setField(data, "circuitBreakerCooldownMinutes", 15L);
            String url = "http://127.0.0.1:" + server.getAddress().getPort();
            for (int i = 0; i < 5; i++) assertThat(data.getData(url + "/missing")).isNull();
            ProviderDiagnostics.begin();
            try {
                assertThatThrownBy(() -> data.getData(url + "/failed")).satisfies(error ->
                        ProviderDiagnostics.record("OPEN_LIBRARY", "Obtener autor", error));
            } finally {
                var details = ProviderDiagnostics.finish();
                assertThat(details).hasSize(1);
                assertThat(details.get(0).getString("code")).isEqualTo("UNAVAILABLE");
                assertThat(details.get(0).getInteger("httpStatus")).isEqualTo(503);
            }
        } finally { server.stop(0); }
    }

}
