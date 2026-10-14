package com.martinia.indigo.common.util;

import com.martinia.indigo.metadata.application.ProviderDiagnostics;
import com.martinia.indigo.metadata.application.reviews.ReviewProviderRequestPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Serial book provider requests, fixed spacing and bounded retries of the failed HTTP operation. */
@Slf4j
@Component
public class BookProviderHttpRequests {
    private static final ThreadLocal<BooleanSupplier> ACTIVE = new ThreadLocal<>();
    private static final ThreadLocal<java.util.function.Consumer<Instant>> WAITING = new ThreadLocal<>();
    private final java.util.concurrent.ConcurrentHashMap<String, State> states = new java.util.concurrent.ConcurrentHashMap<>();
    private static final class State {
        final ReentrantLock lock = new ReentrantLock(true);
        Instant lastRequest;
        Instant retryAfter;
    }

    @Value("${metadata.http.books-retry.attempts:3}")
    private int attempts = 3;
    @Value("${metadata.http.books-retry.initial-delay-millis:1000}")
    private long initialDelayMillis = 1000;
    @Value("${metadata.http.books-retry.max-wait-millis:10000}")
    private long maxWaitMillis = 10000;

    @Value("${metadata.http.rate-limit.initial-delay-seconds:60}")
    private long initialRateLimitDelaySeconds = 60;
    @Value("${metadata.http.rate-limit.max-delay-seconds:900}")
    private long maxRateLimitDelaySeconds = 900;

    public static <T> T whileActive(BooleanSupplier active, Supplier<T> request) {
        return whileActive(active, until -> {}, request);
    }

    public static <T> T whileActive(BooleanSupplier active, java.util.function.Consumer<Instant> waiting, Supplier<T> request) {
        var previousWaiting = WAITING.get();
        WAITING.set(waiting);
        BooleanSupplier previous = ACTIVE.get();
        ACTIVE.set(active);
        try { checkActive(); return request.get(); }
        finally {
            if (previous == null) ACTIVE.remove(); else ACTIVE.set(previous);
            if (previousWaiting == null) WAITING.remove(); else WAITING.set(previousWaiting);
        }
    }

    public String getData(URL url, long minimumIntervalMillis) throws IOException {
        State state = states.computeIfAbsent(url.getHost(), ignored -> new State());
        var lock = state.lock;
        String provider = url.getHost().contains("googleapis.com") ? "GOOGLE_BOOKS" : "OPEN_LIBRARY";
        try {
            while (!lock.tryLock(100, TimeUnit.MILLISECONDS)) checkActive();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Book provider request cancelled");
        }
        try {
            int limit = Math.max(1, Math.min(5, attempts));
            long remainingWait = Math.max(0, maxWaitMillis);
            for (int attempt = 1; attempt <= limit; attempt++) {
                checkActive();
                if (state.retryAfter != null && state.retryAfter.isAfter(Instant.now())) {
                    waitForProvider(provider, state.retryAfter);
                }
                if (state.lastRequest != null) waitUntil(state.lastRequest.plusMillis(Math.max(0, minimumIntervalMillis)));
                checkActive();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "Indigo/0.0.1 (book metadata client)");
                Exception failure;
                try {
                    state.lastRequest = Instant.now();
                    int status = connection.getResponseCode();
                    if (status == 404) { state.retryAfter = null; return null; }
                    if (status < 400) {
                        try (var input = connection.getInputStream()) {
                            String response = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                            state.retryAfter = null;
                            return response.isBlank() ? null : response;
                        }
                    }
                    var http = new RestClientResponseException("HTTP " + status + " from " + url.getHost(),
                            status, connection.getResponseMessage(), null, null, null);
                    // Authentication, forbidden access and malformed requests are not rate limits.
                    if (status != 429 && status != 408 && status < 500) throw http;
                    state.retryAfter = ReviewProviderRequestPolicy.retryAt(connection.getHeaderField("Retry-After"));
                    if (status == 429 && state.retryAfter == null) {
                        state.retryAfter = Instant.now().plusSeconds(Math.min(Math.max(1, maxRateLimitDelaySeconds), Math.max(1, initialRateLimitDelaySeconds) * (1L << (attempt - 1))));
                    }
                    failure = http;
                } catch (IOException exception) {
                    failure = exception;
                } finally { connection.disconnect(); }

                log.warn("Book provider request {}{} failed (attempt {}/{})", url.getHost(), url.getPath(), attempt, limit, failure);
                if (attempt == limit) {
                    if (failure instanceof IOException io) throw io;
                    throw (RuntimeException) failure;
                }
                long delay = Math.min(10000, Math.max(0, initialDelayMillis)) * (1L << (attempt - 1));
                Instant next = Instant.now().plusMillis(delay);
                long wait = Math.max(0, Duration.between(Instant.now(), next).toMillis());
                // The local budget bounds our own backoff, never the server's Retry-After.
                // Releasing this request during a shared pause would turn every next book into an error.
                if (wait > remainingWait) {
                    if (failure instanceof IOException io) throw io;
                    throw (RuntimeException) failure;
                }
                remainingWait -= wait;
                ProviderDiagnostics.event(provider, "Reintentar petición", "WAITING",
                        "Reintento " + (attempt + 1) + " de " + limit + " en " + Math.max(1, (wait + 999) / 1000) + " segundos");
                if (state.retryAfter != null && state.retryAfter.isAfter(next)) waitForProvider(provider, state.retryAfter);
                else waitUntil(next);
            }
            throw new IllegalStateException("Book provider retry attempts exhausted");
        } finally { lock.unlock(); }
    }

    private static void waitForProvider(String provider, Instant until) {
        if (!until.isAfter(Instant.now())) return;
        log.info("Book provider requested Retry-After until {}; retaining the current request", until);
        ProviderDiagnostics.event(provider, "Esperar al proveedor", "WAITING",
                provider + " ha solicitado esperar hasta " + until + "; se conserva la consulta actual");
        var listener = WAITING.get();
        try {
            if (listener != null) listener.accept(until);
            waitUntil(until);
        } finally {
            if (listener != null) listener.accept(null);
        }
    }

    private static void waitUntil(Instant until) {
        while (Instant.now().isBefore(until)) {
            checkActive();
            try { Thread.sleep(Math.min(100, Math.max(1, Duration.between(Instant.now(), until).toMillis()))); }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new CancellationException("Book provider request cancelled");
            }
        }
        checkActive();
    }

    public static void checkActive() {
        if (Thread.currentThread().isInterrupted() || (ACTIVE.get() != null && !ACTIVE.get().getAsBoolean()))
            throw new CancellationException("Book provider request cancelled");
    }
}
