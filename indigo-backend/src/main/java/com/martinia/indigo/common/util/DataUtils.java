package com.martinia.indigo.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DataUtils {

    private final Map<String, ProviderState> providerStates = new ConcurrentHashMap<>();

    @Value("${metadata.http.default-minimum-interval-millis:250}")
    private long defaultMinimumIntervalMillis;

    @Value("${metadata.http.google-minimum-interval-millis:1000}")
    private long googleMinimumIntervalMillis;

    @Value("${metadata.http.openlibrary-minimum-interval-millis:500}")
    private long openLibraryMinimumIntervalMillis;

    @Value("${metadata.http.wikipedia-minimum-interval-millis:2000}")
    private long wikipediaMinimumIntervalMillis;

    @Value("${metadata.http.circuit-breaker.failures:3}")
    private int circuitBreakerFailures;

    @Value("${metadata.http.circuit-breaker.cooldown-minutes:15}")
    private long circuitBreakerCooldownMinutes;

    @Value("${metadata.http.rate-limit.initial-delay-seconds:60}")
    private long initialDelaySeconds = 60;

    @Value("${metadata.http.rate-limit.max-delay-seconds:900}")
    private long maxDelaySeconds = 900;

    public String getData(String _url) {
        try {
            URL url = new URL(_url);
            ProviderState state = providerStates.computeIfAbsent(providerKey(url.getHost()), ignored -> new ProviderState());

            synchronized (state) {
                ensureCircuitIsClosed(url.getHost(), state);
                waitForRateLimit(url.getHost(), state);

                try {
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    connection.setRequestProperty("User-Agent", "Indigo/0.0.1 (book metadata client)");

                    if (connection instanceof java.net.HttpURLConnection http && http.getResponseCode() == 429) {
                        long delay = Math.min(Math.max(1, maxDelaySeconds), Math.max(1, initialDelaySeconds)
                                * (1L << Math.min(state.rateLimitFailures++, 20)));
                        Instant requested = com.martinia.indigo.metadata.application.reviews.ReviewProviderRequestPolicy.retryAt(http.getHeaderField("Retry-After"));
                        state.blockedUntil = requested != null ? requested : Instant.now().plusSeconds(delay);
                        http.disconnect();
                        log.warn("Metadata provider {} returned HTTP 429; paused until {}", url.getHost(), state.blockedUntil);
                        throw new com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException(
                                "HTTP 429 from " + url.getHost() + "; retry at " + state.blockedUntil, state.blockedUntil);
                    }
                    StringBuilder data = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                        String line;
                        while (null != (line = br.readLine())) {
                            data.append(line.trim());
                        }
                    }
                    state.failures = 0;
                    state.rateLimitFailures = 0;
                    state.blockedUntil = null;
                    return data.isEmpty() ? null : data.toString();
                }
                catch (Exception exception) {
                    log.warn("Metadata request to {} failed: {}: {}", url.getHost(),
                            exception.getClass().getSimpleName(), exception.getMessage());
                    if (state.blockedUntil == null) registerFailure(url.getHost(), state);
                    throw exception;
                }
                finally {
                    state.lastRequest = Instant.now();
                }
            }
        }
        catch (Exception exception) {
            throw new IllegalStateException("Could not obtain metadata from " + _url, exception);
        }
    }

    private String providerKey(String host) {
        return host.equals("wikipedia.org") || host.endsWith(".wikipedia.org") ? "wikipedia.org" : host;
    }

    public boolean isWikipediaPaused() {
        ProviderState state = providerStates.get("wikipedia.org");
        if (state == null) return false;
        synchronized (state) { return state.blockedUntil != null && Instant.now().isBefore(state.blockedUntil); }
    }

    public boolean awaitWikipediaAvailable(java.util.function.BooleanSupplier active) {
        Instant announced = null;
        while (active.getAsBoolean()) {
            ProviderState state = providerStates.get("wikipedia.org");
            Instant until = null;
            if (state != null) {
                synchronized (state) { until = state.blockedUntil; }
            }
            if (until == null || !Instant.now().isBefore(until)) return true;
            if (!until.equals(announced)) {
                log.info("Waiting for Wikipedia until {}", until.atZone(java.time.ZoneId.systemDefault()));
                announced = until;
            }
            try { Thread.sleep(Math.min(1000, Math.max(1, Duration.between(Instant.now(), until).toMillis()))); }
            catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private void ensureCircuitIsClosed(final String host, final ProviderState state) {
        if (state.blockedUntil == null) {
            return;
        }
        if (Instant.now().isBefore(state.blockedUntil)) {
            throw new com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException(
                    "Metadata provider " + host + " is paused until " + state.blockedUntil, state.blockedUntil);
        }
        state.failures = 0;
        state.blockedUntil = null;
    }

    private void waitForRateLimit(final String host, final ProviderState state) {
        if (state.lastRequest == null) {
            return;
        }
        long elapsed = Duration.between(state.lastRequest, Instant.now()).toMillis();
        long waitMillis = minimumIntervalFor(host) - elapsed;
        if (waitMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(waitMillis);
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for metadata provider", exception);
        }
    }

    private long minimumIntervalFor(final String host) {
        if (host.contains("googleapis.com")) {
            return googleMinimumIntervalMillis;
        }
        if (host.contains("openlibrary.org")) {
            return openLibraryMinimumIntervalMillis;
        }
        if (host.contains("wikipedia.org")) {
            return wikipediaMinimumIntervalMillis;
        }
        return defaultMinimumIntervalMillis;
    }

    private void registerFailure(final String host, final ProviderState state) {
        state.failures++;
        if (state.failures >= circuitBreakerFailures) {
            state.blockedUntil = Instant.now().plus(Duration.ofMinutes(circuitBreakerCooldownMinutes));
            log.warn("Metadata provider {} circuit opened until {}", host, state.blockedUntil);
        }
    }

    private static final class ProviderState {
        private Instant lastRequest;
        private int failures;
        private int rateLimitFailures;
        private Instant blockedUntil;
    }

}
