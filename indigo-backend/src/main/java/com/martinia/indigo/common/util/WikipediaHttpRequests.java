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

/** Serial Wikipedia requests, fixed spacing and bounded retries of the failed HTTP operation. */
@Slf4j
@Component
public class WikipediaHttpRequests {
    private static final ThreadLocal<BooleanSupplier> ACTIVE = new ThreadLocal<>();
    private static final ThreadLocal<java.util.function.Consumer<Instant>> WAITING = new ThreadLocal<>();
    private final ReentrantLock lock = new ReentrantLock(true);
    private Instant lastRequest;
    private Instant retryAfter;

    @Value("${metadata.http.wikipedia-retry.attempts:3}")
    private int attempts = 3;
    @Value("${metadata.http.wikipedia-retry.initial-delay-millis:1000}")
    private long initialDelayMillis = 1000;
    @Value("${metadata.http.wikipedia-retry.max-wait-millis:10000}")
    private long maxWaitMillis = 10000;

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
        try {
            while (!lock.tryLock(100, TimeUnit.MILLISECONDS)) checkActive();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Wikipedia request cancelled");
        }
        try {
            int limit = Math.max(1, Math.min(5, attempts));
            long remainingWait = Math.max(0, maxWaitMillis);
            for (int attempt = 1; attempt <= limit; attempt++) {
                checkActive();
                if (retryAfter != null && retryAfter.isAfter(Instant.now())) {
                    waitForProvider(retryAfter);
                }
                if (lastRequest != null) waitUntil(lastRequest.plusMillis(Math.max(0, minimumIntervalMillis)));
                checkActive();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "Indigo/0.0.1 (book metadata client)");
                Exception failure;
                try {
                    lastRequest = Instant.now();
                    int status = connection.getResponseCode();
                    if (status == 404) { retryAfter = null; return null; }
                    if (status < 400) {
                        try (var input = connection.getInputStream()) {
                            String response = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                            retryAfter = null;
                            return response.isBlank() ? null : response;
                        }
                    }
                    var http = new RestClientResponseException("HTTP " + status + " from " + url.getHost(),
                            status, connection.getResponseMessage(), null, null, null);
                    // Authentication, forbidden access and malformed requests are not rate limits.
                    if (status != 429 && status != 408 && status < 500) throw http;
                    retryAfter = ReviewProviderRequestPolicy.retryAt(connection.getHeaderField("Retry-After"));
                    failure = http;
                } catch (IOException exception) {
                    failure = exception;
                } finally { connection.disconnect(); }

                log.warn("Wikipedia request {}{} failed (attempt {}/{})", url.getHost(), url.getPath(), attempt, limit, failure);
                if (attempt == limit) {
                    if (failure instanceof IOException io) throw io;
                    throw (RuntimeException) failure;
                }
                long delay = Math.min(10000, Math.max(0, initialDelayMillis)) * (1L << (attempt - 1));
                Instant next = Instant.now().plusMillis(delay);
                long wait = Math.max(0, Duration.between(Instant.now(), next).toMillis());
                // The local budget bounds our own backoff, never the server's Retry-After.
                // Releasing this request during a shared pause would turn every next author into an error.
                if (wait > remainingWait) {
                    if (failure instanceof IOException io) throw io;
                    throw (RuntimeException) failure;
                }
                remainingWait -= wait;
                ProviderDiagnostics.event("WIKIPEDIA", "Reintentar petición", "WAITING",
                        "Reintento " + (attempt + 1) + " de " + limit + " en " + Math.max(1, (wait + 999) / 1000) + " segundos");
                if (retryAfter != null && retryAfter.isAfter(next)) waitForProvider(retryAfter);
                else waitUntil(next);
            }
            throw new IllegalStateException("Wikipedia retry attempts exhausted");
        } finally { lock.unlock(); }
    }

    private static void waitForProvider(Instant until) {
        if (!until.isAfter(Instant.now())) return;
        log.info("Wikipedia requested Retry-After until {}; retaining the current request", until);
        ProviderDiagnostics.event("WIKIPEDIA", "Esperar al proveedor", "WAITING",
                "Wikipedia ha solicitado esperar hasta " + until + "; se conserva el autor actual");
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
                throw new CancellationException("Wikipedia request cancelled");
            }
        }
        checkActive();
    }

    private static void checkActive() {
        if (Thread.currentThread().isInterrupted() || (ACTIVE.get() != null && !ACTIVE.get().getAsBoolean()))
            throw new CancellationException("Wikipedia request cancelled");
    }
}
