package com.martinia.indigo.metadata.application.reviews;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.time.Instant;
import java.util.Locale;

/** Detects restrictions even when a provider responds with HTTP 200. */
public final class ReviewPageGuard {
	private ReviewPageGuard() { }

	public static void check(final HtmlPage page) {
		if (page == null) {
			throw new IllegalStateException("Review provider returned no page");
		}
		String path = page.getUrl().getPath().toLowerCase(Locale.ROOT);
		String title = page.getTitleText().toLowerCase(Locale.ROOT);
		if (path.contains("/ap/signin") || path.contains("/user/sign_in")
				|| path.contains("/auth/portal") || path.contains("/errors/validatecaptcha")
				|| title.contains("robot check") || title.contains("captcha")
				|| title.contains("just a moment") || title.contains("access denied")
				|| !page.getByXPath("//input[@id='captchacharacters' or @name='captcha'] | //form[contains(@action,'validateCaptcha') or @id='challenge-form']").isEmpty()) {
			throw new AccessRestrictedException("Review provider requires authentication or a CAPTCHA", null);
		}
	}

	public static class AccessRestrictedException extends IllegalStateException {
		private final Instant retryAt;
		public AccessRestrictedException(final String message, final Instant retryAt) {
			super(message);
			this.retryAt = retryAt;
		}
		public Instant retryAt() { return retryAt; }
	}
}
