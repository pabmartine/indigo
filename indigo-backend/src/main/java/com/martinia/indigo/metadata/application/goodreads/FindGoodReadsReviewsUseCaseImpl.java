package com.martinia.indigo.metadata.application.goodreads;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlArticle;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.common.domain.model.Review;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.application.reviews.ReviewProviderRequestPolicy;
import com.martinia.indigo.metadata.application.reviews.ReviewPageGuard;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.DetectLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsReviewsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.text.Normalizer;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(name = "flags.goodreads-reviews", havingValue = "true")
public class FindGoodReadsReviewsUseCaseImpl implements FindGoodReadsReviewsUseCase {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

	@Value("${metadata.goodreads.reviews}")
	private String endpoint;

	@Resource
	private WebClient webClient;
	@Resource
	private ReviewProviderRequestPolicy reviewProviderRequestPolicy;

	@Resource
	private Optional<DetectLibreTranslatePort> detectLibreTranslatePort;

	@Resource
	private Optional<TranslateLibreTranslatePort> translateLibreTranslatePort;

	@Override
	public List<Review> getReviews(String lang, String title, List<String> authors) {
		if (StringUtils.isBlank(title)) {
			return Collections.emptyList();
		}
		final String author = Optional.ofNullable(authors).orElse(Collections.emptyList()).stream().collect(Collectors.joining(" "));
		return reviewProviderRequestPolicy.getReviews("goodreads", lang + '|' + normalize(title) + '|' + normalize(author), () -> {
			try {
				synchronized (webClient) {
					String path = getPath(title, author);
					if (path != null) {
						return getReviews(lang, path, 1);
					}
				}
				return Collections.emptyList();
			}
			catch (Exception ex) {
				throw new IllegalStateException("Could not obtain Goodreads reviews for " + title + ": " + ex.getMessage(), ex);
			}
		});
	}

	private String getPath(String title, String author) throws Exception {

		String path = null;

		String tokenized_title = normalize(title);
		String tokenized_author = normalize(author);
		String expectedTitle = normalize(title);

		String url = endpoint.replace("$title", encodeQuery(tokenized_title)).replace("$author", encodeQuery(tokenized_author));
		HtmlPage page = webClient.getPage(url);
		ReviewPageGuard.check(page);

		for (Object item : page.getByXPath("//a[contains(@class, 'bookTitle')]")) {
			HtmlAnchor htmlAnchor = (HtmlAnchor) item;
			try {
				String ref = htmlAnchor.getHrefAttribute();
				String candidateTitle = normalize(htmlAnchor.asNormalizedText());
				DomNode row = htmlAnchor.getFirstByXPath("ancestor::tr[1]");
				boolean authorMatches = author.isBlank() || (row != null && row.getByXPath(".//a[contains(@class, 'authorName')]")
						.stream().anyMatch(node -> normalize(((DomNode) node).asNormalizedText()).equals(tokenized_author)));
				if (path == null && authorMatches && (candidateTitle.equals(expectedTitle) || candidateTitle.startsWith(expectedTitle + "+"))) {
					path = ref;
				}
			}
			catch (Exception e) {
				log.debug("Could not evaluate Goodreads search result", e);
			}
		}

		return path;
	}

	private static String normalize(String title) {
		int openingParenthesis = title.indexOf("(");
		int closingParenthesis = title.indexOf(")", openingParenthesis + 1);
		if (openingParenthesis >= 0 && closingParenthesis > openingParenthesis) {
			title = title.substring(0, openingParenthesis) + title.substring(closingParenthesis + 1);
		}
		return Normalizer.normalize(title, Normalizer.Form.NFD).toLowerCase(Locale.ROOT).replaceAll("[^\\p{ASCII}]", "").replaceAll(" ", "+")
				.replaceAll(",", "").replaceAll("\\.", "+").replaceAll(":", "+").replaceAll("\\+\\+", "+");
	}

	private static String encodeQuery(final String value) {
		return java.net.URLEncoder.encode(value.replace('+', ' '), java.nio.charset.StandardCharsets.UTF_8);
	}

	private List<Review> getReviews(final String lang, final String url, final int numPage) throws Exception {

		final List<Review> reviews = new ArrayList<>();

		final HtmlPage page = webClient.getPage(URI.create(endpoint).resolve(url).toString());
		ReviewPageGuard.check(page);

		final List<Review> foreignComments = new ArrayList<>();
		List<?> cards = page.getByXPath("//article[contains(concat(' ', normalize-space(@class), ' '), ' ReviewCard ')]");
		cards.stream().limit(30).takeWhile(data -> reviews.size() < 10).forEach(item -> {
			HtmlArticle htmlArticle = (HtmlArticle) item;
			try {
				final DomNode nameNode = firstNode(htmlArticle,
						".//div[contains(@class, 'ReviewerProfile__name')]//a",
						".//div[contains(@class, 'User__name')]//a");
				final String name = nameNode == null ? null : nameNode.asNormalizedText();
				final DomNode ratingNode = firstNode(htmlArticle,
						".//*[@role='img' and starts-with(@aria-label, 'Rating ')]",
						".//span[contains(@class, 'RatingStatistics')]");
				final String strRating = ratingNode == null || ratingNode.getAttributes().getNamedItem("aria-label") == null
						? null
						: ratingNode.getAttributes().getNamedItem("aria-label").getNodeValue();

				if (name != null && strRating != null) {
					final Matcher matcher = Pattern.compile("\\d+").matcher(strRating);
					if (!matcher.find()) {
						return;
					}
					int rating = Integer.valueOf(matcher.group());
					String title = "";
					DomNode dateNode = firstNode(htmlArticle,
							".//section[contains(@class, 'ReviewCard__row')]//a[contains(@href, '/review/show/')][1]",
							".//a[contains(@class, 'ReviewCard__timestamp')]");
					String strDate = dateNode == null ? null : dateNode.asNormalizedText();
					LocalDate localDate = LocalDate.parse(strDate, DATE_FORMATTER);
					Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
					final String comment = Optional.ofNullable(htmlArticle.getFirstByXPath(
							".//section[contains(@class, 'ReviewText__content')]//*[contains(concat(' ', normalize-space(@class), ' '), ' Formatted ')]"))
						.map(node -> ((DomNode) node).asNormalizedText()).orElse(null);

					final String language = comment == null || comment.isBlank() ? null : com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation.normalizeLanguage(detectLibreTranslatePort.map(libreTranslate -> libreTranslate.detect(comment)).orElse(null));

					final Review review = Review.builder().comment(comment).name(name).date(date).rating(rating).title(title)
						.lastMetadataSync(new Date()).provider(ProviderEnum.GOODREADS.name())
						.originalLanguage(language).language(language)
						.sourceUrl(dateNode instanceof HtmlAnchor anchor
								? page.getFullyQualifiedUrl(anchor.getHrefAttribute()).toString() : page.getUrl().toString()).build();
					if (language != null && !language.equals(com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation.normalizeLanguage(lang))) {
						foreignComments.add(review);
					}
					else {
						reviews.add(review);
					}
				}
			}
			catch (Exception e) {
				log.debug("Could not parse Goodreads review", e);
			}
		});

		log.debug("Number of reviews: {}, foreign: {}", reviews.size(), foreignComments.size());
		if (reviews.size() < 10 && !CollectionUtils.isEmpty(foreignComments)) {
			for (Review review : foreignComments) {
			final String translated = translateLibreTranslatePort.map(libreTranslate -> libreTranslate.translate(review.getComment(), lang))
					.orElse(null);
			if (StringUtils.isNotBlank(translated)) {
				review.setComment(translated);
				review.setLanguage(lang);
			}
				reviews.add(review);
				if (reviews.size() == 10) {
					break;
				}
			}
		}

		if (!cards.isEmpty() && reviews.isEmpty()) {
			throw new IllegalStateException("Goodreads review cards were found but none could be parsed");
		}
		return reviews;
	}

	private static DomNode firstNode(final HtmlArticle article, final String... expressions) {
		for (String expression : expressions) {
			final DomNode node = article.getFirstByXPath(expression);
			if (node != null) {
				return node;
			}
		}
		return null;
	}

}
