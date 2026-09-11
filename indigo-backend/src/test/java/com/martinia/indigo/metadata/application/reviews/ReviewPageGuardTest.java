package com.martinia.indigo.metadata.application.reviews;

import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class ReviewPageGuardTest {
	@Test
	void persistsCooldownAndReloadsItInANewPolicyInstance() {
		var mongo = org.mockito.Mockito.mock(org.springframework.data.mongodb.core.MongoTemplate.class);
		var first = new ReviewProviderRequestPolicy();
		ReflectionTestUtils.setField(first, "mongoTemplate", mongo);
		ReflectionTestUtils.setField(first, "circuitBreakerCooldownMinutes", 30L);
		assertThrows(IllegalStateException.class, () -> first.getReviews("amazon", "book", () -> {
			throw new ReviewPageGuard.AccessRestrictedException("login required", null);
		}));
		var update = org.mockito.ArgumentCaptor.forClass(org.springframework.data.mongodb.core.query.Update.class);
		org.mockito.Mockito.verify(mongo).upsert(org.mockito.ArgumentMatchers.any(org.springframework.data.mongodb.core.query.Query.class), update.capture(), org.mockito.ArgumentMatchers.eq("reviewProviderStates"));
		java.util.Date until = update.getValue().getUpdateObject().get("$max", org.bson.Document.class).getDate("blockedUntil");
		org.mockito.Mockito.when(mongo.findById("amazon", org.bson.Document.class, "reviewProviderStates"))
				.thenReturn(new org.bson.Document("_id", "amazon").append("blockedUntil", until));
		var restarted = new ReviewProviderRequestPolicy();
		ReflectionTestUtils.setField(restarted, "mongoTemplate", mongo);
		assertThrows(IllegalStateException.class, () -> restarted.getReviews("amazon", "other", () -> {
			fail("A restarted process must not call the paused provider"); return Collections.emptyList();
		}));
	}

	@Test
	void rateLimitHonorsRetryAfterWithoutRetrying() throws Exception {
		var response = new com.gargoylesoftware.htmlunit.WebResponse(
				new com.gargoylesoftware.htmlunit.WebResponseData("limit".getBytes(java.nio.charset.StandardCharsets.UTF_8), 429, "Too Many Requests",
						java.util.List.of(new com.gargoylesoftware.htmlunit.util.NameValuePair("Retry-After", "7200"))),
				new com.gargoylesoftware.htmlunit.WebRequest(new java.net.URL("https://www.amazon.es/")), 0);
		var policy = new ReviewProviderRequestPolicy();
		ReflectionTestUtils.setField(policy, "maxAttempts", 3);
		AtomicInteger calls = new AtomicInteger();
		assertThrows(IllegalStateException.class, () -> policy.getReviews("amazon", "a", () -> {
			calls.incrementAndGet();
			throw new IllegalStateException(new com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException(response));
		}));
		assertEquals(1, calls.get());
		@SuppressWarnings("unchecked")
		var pauses = (java.util.Map<String, java.time.Instant>) ReflectionTestUtils.getField(policy, "blockedUntil");
		assertTrue(pauses.get("amazon").isAfter(java.time.Instant.now().plusSeconds(7190)));
	}

	@Test
	void recognizesTheAmazonSigninRedirect() throws Exception {
		try (WebClient client = new WebClient()) {
			var connection = new com.gargoylesoftware.htmlunit.MockWebConnection();
			var url = new java.net.URL("https://www.amazon.es/ap/signin");
			connection.setResponse(url, "<html><title>Amazon Sign In</title></html>");
			client.setWebConnection(connection);
			com.gargoylesoftware.htmlunit.html.HtmlPage page = client.getPage(url);
			assertThrows(ReviewPageGuard.AccessRestrictedException.class, () -> ReviewPageGuard.check(page));
		}
	}
	@Test
	void detectsCaptchaEvenWithSuccessfulHttpStatus() throws Exception {
		try (WebClient client = new WebClient()) {
			var page = client.loadHtmlCodeIntoCurrentWindow("<html><title>Amazon</title><form action='/errors/validateCaptcha'><input id='captchacharacters'></form></html>");
			assertThrows(ReviewPageGuard.AccessRestrictedException.class, () -> ReviewPageGuard.check(page));
		}
	}

	@Test
	void doesNotMistakeAReviewMentioningCaptchaForABlock() throws Exception {
		try (WebClient client = new WebClient()) {
			var page = client.loadHtmlCodeIntoCurrentWindow("<html><title>Book</title><article>This book explains captcha and robot check systems.</article></html>");
			assertDoesNotThrow(() -> ReviewPageGuard.check(page));
		}
	}

	@Test
	void restrictedResponsesAreNeverRetriedOrCachedAsEmpty() {
		var policy = new ReviewProviderRequestPolicy();
		ReflectionTestUtils.setField(policy, "maxAttempts", 3);
		AtomicInteger requests = new AtomicInteger();
		assertThrows(IllegalStateException.class, () -> policy.getReviews("amazon", "a", () -> {
			requests.incrementAndGet();
			throw new IllegalStateException("wrapper", new ReviewPageGuard.AccessRestrictedException("captcha", null));
		}));
		assertThrows(IllegalStateException.class, () -> policy.getReviews("amazon", "b", () -> {
			requests.incrementAndGet(); return Collections.emptyList();
		}));
		assertEquals(1, requests.get());
	}

	@Test
	void parsesBothRetryAfterFormatsAndInvalidValues() {
		assertTrue(ReviewProviderRequestPolicy.retryAt("3600").isAfter(java.time.Instant.now().plusSeconds(3590)));
		assertEquals(java.time.Instant.parse("2030-01-01T00:00:00Z"), ReviewProviderRequestPolicy.retryAt("Tue, 1 Jan 2030 00:00:00 GMT"));
		assertNull(ReviewProviderRequestPolicy.retryAt("invalid"));
	}
}
