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
                assertThatThrownBy(() -> data.getData(url)).hasRootCauseInstanceOf(java.io.IOException.class);
            }
            ProviderDiagnostics.begin();
            try {
                try { data.getData(url); fail("Expected a paused provider"); }
                catch (IllegalStateException error) {
                    ProviderDiagnostics.record("WIKIPEDIA", "Obtener autor", error);
                }
            } finally {
                var diagnostics = ProviderDiagnostics.finish();
                assertThat(diagnostics).hasSize(1);
                assertThat(diagnostics.get(0).getString("code")).isEqualTo("PAUSED");
                assertThat(diagnostics.get(0).getDate("retryAt")).isNotNull();
            }
            assertThat(requests.get()).isEqualTo(3);
        } finally { server.stop(0); }
    }
}
