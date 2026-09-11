package com.martinia.indigo.metadata.application.reviews;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewProviderRequestPolicyTest {

	@Test
	void getReviews_CachesSuccessfulProviderResponses() {
		ReviewProviderRequestPolicy policy = new ReviewProviderRequestPolicy();
		ReflectionTestUtils.setField(policy, "cacheTtlMinutes", 60L);
		ReflectionTestUtils.setField(policy, "maxAttempts", 1);
		AtomicInteger calls = new AtomicInteger();

		policy.getReviews("amazon", "book", () -> {
			calls.incrementAndGet();
			return Collections.emptyList();
		});
		policy.getReviews("amazon", "book", () -> {
			calls.incrementAndGet();
			return Collections.emptyList();
		});

		assertEquals(1, calls.get());
	}

	@Test
	void getReviews_OpensCircuitAfterRepeatedProviderFailures() {
		ReviewProviderRequestPolicy policy = new ReviewProviderRequestPolicy();
		ReflectionTestUtils.setField(policy, "maxAttempts", 1);
		ReflectionTestUtils.setField(policy, "circuitBreakerFailures", 1);
		ReflectionTestUtils.setField(policy, "circuitBreakerCooldownMinutes", 60L);
		AtomicInteger calls = new AtomicInteger();

		assertThrows(IllegalStateException.class, () -> policy.getReviews("goodreads", "first", () -> {
			calls.incrementAndGet();
			throw new IllegalStateException("blocked");
		}));
		assertThrows(IllegalStateException.class, () -> policy.getReviews("goodreads", "second", () -> {
			calls.incrementAndGet();
			return Collections.emptyList();
		}));

		assertEquals(1, calls.get());
	}
}
