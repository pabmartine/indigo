package com.martinia.indigo.metadata.application.amazon;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.common.domain.model.Review;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.application.reviews.ReviewProviderRequestPolicy;
import com.martinia.indigo.metadata.application.reviews.ReviewPageGuard;
import com.martinia.indigo.metadata.domain.ports.usecases.amazon.FindAmazonReviewsUseCase;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(name = "flags.amazon", havingValue = "true")
@Transactional
public class FindAmazonReviewsUseCaseImpl implements FindAmazonReviewsUseCase {

	private static final String DATE_PATTERN = "d MMMM yyyy";

	@Value("${metadata.amazon.asin}")
	private String endpointAsin;

	@Value("${metadata.amazon.reviews}")
	private String endpointReviews;

	@Resource
	private WebClient webClient;
	@Resource
	private ReviewProviderRequestPolicy reviewProviderRequestPolicy;
	@Resource
	private com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation spanishTranslation;

	@Override
	public List<Review> getReviews(String title, List<String> authors) {
		if (StringUtils.isBlank(title)) {
			return Collections.emptyList();
		}

		final String author = authors == null ? "" : authors.stream().collect(Collectors.joining(" "));
		return reviewProviderRequestPolicy.getReviews("amazon", normalize(title) + '|' + normalize(author), () -> {
			try {
				synchronized (webClient) {
					String asin = getAsin(title, author);
					if (asin != null) {
						return getReviews(asin, 1);
					}
				}
				return Collections.emptyList();
			}
			catch (Exception ex) {
				throw new IllegalStateException("Could not obtain Amazon reviews for " + title + ": " + ex.getMessage(), ex);
			}
		});
	}

	private String getAsin(String title, String author) throws Exception {

		AtomicReference<String> asin = new AtomicReference<>();

		String tokenized_title = normalize(title);
		String tokenized_author = normalize(author);

		String searchUrl = endpointAsin.replace("$title", encodeQuery(tokenized_title)).replace("$author", encodeQuery(tokenized_author));
		log.debug("Search URL: " + searchUrl);
		HtmlPage page = webClient.getPage(searchUrl);
		if (page == null) {
			return null;
		}
		ReviewPageGuard.check(page);

		page.getByXPath("//div[@data-component-type='s-search-result']").stream().forEach(item -> {
			try {
				HtmlDivision searchResult = (HtmlDivision) item;
				String compareAsin = searchResult.getAttribute("data-asin");
				String compareTitle = firstText(searchResult, ".//h2//span[1]", ".//a//span[1]");
				if (StringUtils.isBlank(compareAsin) || StringUtils.isBlank(compareTitle)) {
					return;
				}
				String filter = StringUtils.stripAccents(title).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").toLowerCase(Locale.ROOT)
						.trim();

				String candidate = StringUtils.stripAccents(compareTitle).toLowerCase(Locale.ROOT)
						.replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim();
				String authorText = StringUtils.stripAccents(searchResult.asNormalizedText()).toLowerCase(Locale.ROOT)
						.replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ");
				String expectedAuthor = StringUtils.stripAccents(author).toLowerCase(Locale.ROOT)
						.replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim();
				boolean authorMatches = expectedAuthor.isBlank() || authorText.contains(expectedAuthor);
				if (asin.get() == null && (candidate.equals(filter) || candidate.startsWith(filter + " ")) && authorMatches) {
					asin.set(compareAsin);
				}
			}
			catch (Exception e) {
				log.error(e.getMessage());
			}
		});

		return asin.get();
	}

	private static String normalize(String title) {
		int openingParenthesis = title.indexOf("(");
		int closingParenthesis = title.indexOf(")", openingParenthesis + 1);
		if (openingParenthesis >= 0 && closingParenthesis > openingParenthesis) {
			title = title.substring(0, openingParenthesis) + title.substring(closingParenthesis + 1);
		}
		return Normalizer.normalize(title, Normalizer.Form.NFD).toLowerCase(Locale.ROOT).replaceAll("[^\\p{ASCII}]", "").replaceAll(" ", "+")
				.replaceAll(",", "").replaceAll("-", " ").replaceAll("\\.", "+").replaceAll(":", "+").replaceAll("\\+\\+", "+");
	}

	private static String encodeQuery(final String value) {
		return java.net.URLEncoder.encode(value.replace('+', ' '), java.nio.charset.StandardCharsets.UTF_8);
	}

	private List<Review> getReviews(String asin, int numPage) throws Exception {

		List<Review> reviews = new ArrayList<>();

		String url = endpointReviews.replace("$asin", asin).replace("$numPage", String.valueOf(numPage));
		HtmlPage page;
		try {
			page = webClient.getPage(url);
		}
		catch (FailingHttpStatusCodeException exception) {
			if (exception.getStatusCode() != 404 || !URI.create(url).getPath().startsWith("/product-reviews/")) {
				throw exception;
			}
			// Some marketplaces no longer expose the public review listing. The product
			// page may still contain a small public selection of reviews.
			page = webClient.getPage(URI.create(url).resolve("/dp/" + asin).toString());
		}
		ReviewPageGuard.check(page);

		List<?> cards = page.getByXPath("//div[@data-hook='review'] | //div[contains(concat(' ', normalize-space(@class), ' '), ' review ')]");
		cards.stream().limit(10).forEach(item -> {
			HtmlDivision htmlDivision = (HtmlDivision) item;
			try {
				String name = firstText(htmlDivision, ".//*[@data-hook='review-author' or contains(@class, 'a-profile-name')][1]", "(.//span)[1]");
				String ratingText = firstText(htmlDivision,
						".//*[@data-hook='review-star-rating' or @data-hook='cmps-review-star-rating' or contains(@class, 'a-star')][1]");
				String title = firstText(htmlDivision, ".//*[@data-hook='review-title'][1]");
				String strDate = firstText(htmlDivision, ".//*[@data-hook='review-date'][1]", ".//span[contains(., 'Reviewed')][1]");
				String comment = firstText(htmlDivision, ".//*[@data-hook='review-body'][1]", "(.//span)[last()]");
				if (StringUtils.isBlank(name) || StringUtils.isBlank(ratingText) || StringUtils.isBlank(strDate)) {
					return;
				}
				Matcher ratingMatcher = Pattern.compile("\\d+").matcher(ratingText);
				if (!ratingMatcher.find()) {
					return;
				}
				int rating = Integer.parseInt(ratingMatcher.group());
				Matcher matcher = Pattern.compile("\\d+").matcher(strDate);
				if (!matcher.find()) {
					return;
				}
				int value = Integer.valueOf(matcher.group());
				strDate = strDate.substring(strDate.indexOf(String.valueOf(value)), strDate.length()).replaceAll("de ", "");
				Date date = parseReviewDate(strDate);

				reviews.add(Review.builder().comment(comment).name(name).date(date).rating(rating).title(title).lastMetadataSync(new Date())
							.sourceUrl(StringUtils.isBlank(htmlDivision.getId()) ? url
									: URI.create(url).resolve("/gp/customer-reviews/" + htmlDivision.getId()).toString())
							.originalLanguage(StringUtils.trimToNull(htmlDivision.getAttribute("lang")))
							.language(StringUtils.trimToNull(htmlDivision.getAttribute("lang")))
							.provider(ProviderEnum.AMAZON.name()).build());
			}
			catch (Exception e) {
				log.error(e.getMessage());
			}
		});

		if (!cards.isEmpty() && reviews.isEmpty()) {
			throw new IllegalStateException("Amazon review cards were found but none could be parsed");
		}
		for (Review review : reviews) {
			var translated = spanishTranslation.translate(review.getComment(), review.getOriginalLanguage());
			review.setComment(translated.text());
			review.setOriginalLanguage(translated.originalLanguage());
			review.setLanguage(translated.language());
			// Titles can have a different language from the review body.
			var translatedTitle = spanishTranslation.translate(review.getTitle(), null);
			review.setTitle(translatedTitle.text());
		}
		return reviews;
	}

	static Date parseReviewDate(final String value) {
		for (Locale locale : List.of(Locale.forLanguageTag("es"), Locale.ENGLISH)) {
			try {
				LocalDate date = LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern(DATE_PATTERN, locale));
				return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
			}
			catch (java.time.format.DateTimeParseException ignored) { }
		}
		throw new IllegalArgumentException("Unsupported Amazon review date: " + value);
	}

	private String firstText(final HtmlDivision review, final String... xpaths) {
		for (String xpath : xpaths) {
			Object node = review.getFirstByXPath(xpath);
			if (node instanceof DomNode domNode) {
				String text = domNode.asNormalizedText();
				if (StringUtils.isNotBlank(text)) {
					return text;
				}
			}
		}
		return null;
	}
}
