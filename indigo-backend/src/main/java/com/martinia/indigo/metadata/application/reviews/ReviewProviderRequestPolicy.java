package com.martinia.indigo.metadata.application.reviews;

import com.martinia.indigo.common.domain.model.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

@Component
@Slf4j
public class ReviewProviderRequestPolicy {
	@Autowired(required = false)
	private org.springframework.beans.factory.ObjectProvider<ReviewQueueService> reviewQueue;
	@Autowired(required = false)
	private MongoTemplate mongoTemplate;

	@Value("${metadata.reviews.cache-ttl-minutes:360}")
	private long cacheTtlMinutes;

	@Value("${metadata.reviews.max-attempts:2}")
	private int maxAttempts;

	@Value("${metadata.reviews.retry-delay-millis:1000}")
	private long retryDelayMillis;

	@Value("${metadata.reviews.amazon.minimum-interval-millis:2000}")
	private long amazonMinimumIntervalMillis;

	@Value("${metadata.reviews.goodreads.minimum-interval-millis:1500}")
	private long goodreadsMinimumIntervalMillis;
	@Value("${metadata.reviews.circuit-breaker.failures:3}")
	private int circuitBreakerFailures;
	@Value("${metadata.reviews.circuit-breaker.cooldown-minutes:30}")
	private long circuitBreakerCooldownMinutes;

	private final Map<String, CachedReviews> cache = new ConcurrentHashMap<>();
	private final Map<String, Instant> lastRequest = new ConcurrentHashMap<>();
	private final Map<String, Object> providerLocks = new ConcurrentHashMap<>();
	private final Map<String, Integer> consecutiveFailures = new ConcurrentHashMap<>();
	private final Map<String, Instant> blockedUntil = new ConcurrentHashMap<>();

	public List<Review> getReviews(final String provider, final String cacheKey, final Supplier<List<Review>> request) {
		final String key = provider + ':' + cacheKey;
		final CachedReviews cachedReviews = cache.get(key);
		if (cachedReviews != null && cachedReviews.expiresAt().isAfter(Instant.now())) {
			return cachedReviews.reviews();
		}

		synchronized (providerLocks.computeIfAbsent(provider, ignored -> new Object())) {
			ensureCircuitClosed(provider);
			final CachedReviews refreshedCache = cache.get(key);
			if (refreshedCache != null && refreshedCache.expiresAt().isAfter(Instant.now())) {
				return refreshedCache.reviews();
			}
			for (int attempt = 1; attempt <= Math.max(1, maxAttempts); attempt++) {
				try {
					final List<Review> reviews = List.copyOf(request.get());
					cache.put(key, new CachedReviews(reviews, Instant.now().plus(Duration.ofMinutes(cacheTtlMinutes))));
					consecutiveFailures.remove(provider);
					blockedUntil.remove(provider);
					return reviews;
				}
				catch (RuntimeException ex) {
					ReviewQueueService.rethrowCancellation(ex);
					for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
						if (cause instanceof ReviewPageGuard.AccessRestrictedException restricted) {
							pause(provider, restricted.retryAt());
							throw ex;
						}
						if (cause instanceof FailingHttpStatusCodeException http) {
							int status = http.getStatusCode();
							if (status == 401 || status == 403 || status == 429 || status == 503) {
								pause(provider, retryAt(http.getResponse().getResponseHeaderValue("Retry-After")));
								throw ex;
							}
							if (status >= 400 && status < 500) {
								throw ex;
							}
						}
					}
					if (attempt == Math.max(1, maxAttempts)) {
						registerFailure(provider);
						log.warn("Review request to {} failed after {} attempts", provider, attempt, ex);
						throw new IllegalStateException("Review provider " + provider + " failed after " + attempt + " attempts", ex);
					}
					waitBeforeRetry(provider, attempt);
				}
			}
		}
		return Collections.emptyList();
	}

	private void ensureCircuitClosed(final String provider) {
		if (mongoTemplate != null) {
			Document state = mongoTemplate.findById(provider, Document.class, "reviewProviderStates");
			if (state != null && state.getDate("blockedUntil") != null
					&& state.getDate("blockedUntil").toInstant().isAfter(Instant.now())) {
				blockedUntil.put(provider, state.getDate("blockedUntil").toInstant());
			}
		}
		final Instant until = blockedUntil.get(provider);
		if (until != null && until.isAfter(Instant.now())) {
			throw new ReviewPageGuard.AccessRestrictedException("Review provider " + provider + " is temporarily paused until " + until, until);
		}
		if (until != null) {
			blockedUntil.remove(provider);
			consecutiveFailures.remove(provider);
		}
	}

	private void registerFailure(final String provider) {
		final int failures = consecutiveFailures.merge(provider, 1, Integer::sum);
		if (failures >= Math.max(1, circuitBreakerFailures)) {
			pause(provider, null);
		}
	}

	private void pause(final String provider, final Instant requested) {
		Instant minimum = Instant.now().plus(Duration.ofMinutes(Math.max(1, circuitBreakerCooldownMinutes)));
		Instant until = requested != null && requested.isAfter(minimum) ? requested : minimum;
		blockedUntil.put(provider, until);
		if (mongoTemplate != null) {
			mongoTemplate.upsert(new Query(Criteria.where("_id").is(provider)),
					new Update().max("blockedUntil", Date.from(until)), "reviewProviderStates");
		}
		log.warn("Review provider {} paused until {}", provider, until);
	}

	public static Instant retryAt(final String value) {
		if (value == null) { return null; }
		try { return Instant.now().plusSeconds(Math.max(0, Long.parseLong(value.trim()))); }
		catch (RuntimeException ignored) {
			try { return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant(); }
			catch (RuntimeException invalid) { return null; }
		}
	}

	public void throttle(final String provider) {
		if (reviewQueue != null) {
			reviewQueue.getObject().awaitPermit(provider);
			ensureCircuitClosed(provider);
			return;
		}
		final long minimumInterval = "amazon".equals(provider) ? amazonMinimumIntervalMillis : goodreadsMinimumIntervalMillis;
		final Instant now = Instant.now();
		final Instant previous = lastRequest.get(provider);
		if (previous != null) {
			sleep(Math.max(0, minimumInterval - Duration.between(previous, now).toMillis()));
		}
		lastRequest.put(provider, Instant.now());
	}

	private void waitBeforeRetry(final String provider, final int attempt) {
		final long exponentialDelay = retryDelayMillis * (1L << (attempt - 1));
		final long jitter = Math.round(Math.random() * Math.max(1, retryDelayMillis / 4));
		log.debug("Retrying {} review request after {} ms", provider, exponentialDelay + jitter);
		sleep(exponentialDelay + jitter);
	}

	private static void sleep(final long delayMillis) {
		if (delayMillis <= 0) {
			return;
		}
		try {
			Thread.sleep(delayMillis);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Review request interrupted", ex);
		}
	}

	private record CachedReviews(List<Review> reviews, Instant expiresAt) {
	}
}
