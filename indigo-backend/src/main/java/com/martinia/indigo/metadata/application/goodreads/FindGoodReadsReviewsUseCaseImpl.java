package com.martinia.indigo.metadata.application.goodreads;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlArticle;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.common.model.Review;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsReviewsUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.DetectLibreTranslateUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.TranslateLibreTranslateUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FindGoodReadsReviewsUseCaseImpl implements FindGoodReadsReviewsUseCase {

	private static SimpleDateFormat SDF = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

	private String endpoint = "https://www.goodreads.com/";

	private String PROVIDER = "Goodreads";

	@Resource
	private DetectLibreTranslateUseCase detectLibreTranslateUseCase;

	@Resource
	private TranslateLibreTranslateUseCase translateLibreTranslateUseCase;

	@Override
	public List<Review> getReviews(String lang, String title, List<String> authors) {

		List<Review> listReviews = null;
		try {
			String path = getPath(title, authors.stream().collect(Collectors.joining(" ")));
			if (path != null) {
				listReviews = getReviews(lang, path, 1);
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
		return listReviews;
	}

	private String getPath(String title, String author) throws Exception {

		AtomicReference<String> path = new AtomicReference<>();

		WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);

		String tokenized_title = normalize(title);
		String tokenized_author = normalize(author);

		String url = "https://www.goodreads.com/search?q=" + tokenized_title + "+" + tokenized_author + "&search_type=books";
		HtmlPage page = webClient.getPage(url);

		page.getByXPath("//a[@class='bookTitle']").stream().forEach(item -> {
			HtmlAnchor htmlAnchor = (HtmlAnchor) item;
			try {
				String ref = htmlAnchor.getHrefAttribute();
				String compareTitle = htmlAnchor.getFirstChild().getNextSibling().getFirstChild().asText();
				String[] terms = normalize(compareTitle).split("\\+");
				String filter = StringUtils.stripAccents(title).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").toLowerCase()
						.trim();

				//TODO: mejorar esto.. en amazon los titulos son algo especiales por lo que no vale la comprobación de todas las palabras.. debería buscar algo intermedio
				boolean similarContains = true;
				for (String term : terms) {
					term = StringUtils.stripAccents(term).toLowerCase().trim();
					if (!filter.contains(term)) {
						similarContains = false;
					}
				}

				long hasTerms = Arrays.stream(terms).filter(term -> {
					return (filter.contains(StringUtils.stripAccents(term).toLowerCase().trim()));
				}).count();

				if ((path == null || path.get() == null) && (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1)) {
					path.set(ref);
				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		});

		webClient.close();

		return path.get();
	}

	private static String normalize(String title) {
		if (title.contains("(")) {
			title = title.substring(0, title.indexOf("(")) + title.substring(title.indexOf(")") + 1, title.length());
		}
		return Normalizer.normalize(title, Normalizer.Form.NFD).toLowerCase().replaceAll("[^\\p{ASCII}]", "").replaceAll(" ", "+")
				.replaceAll(",", "").replaceAll("\\.", "+").replaceAll(":", "+").replaceAll("\\+\\+", "+");
	}

	private List<Review> getReviews(String lang, String url, int numPage) throws Exception {

		List<Review> reviews = new ArrayList<>();

		WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);

		HtmlPage page = webClient.getPage("https://www.goodreads.com" + url);

		List<Review> foreignComments = new ArrayList<>();
		page.getByXPath("//article[@class='ReviewCard']").stream().takeWhile(data -> reviews.size() < 10).forEach(item -> {
			HtmlArticle htmlArticle = (HtmlArticle) item;
			try {
				String name = htmlArticle.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild().getFirstChild()
						.getFirstChild().asText();
				String strRating = htmlArticle.getFirstChild().getNextSibling().getFirstChild().getFirstChild().getFirstChild()
						.getAttributes().getNamedItem("aria-label").getNodeValue();
				Matcher matcher = Pattern.compile("\\d+").matcher(strRating);
				matcher.find();
				int rating = Integer.valueOf(matcher.group());
				String title = "";
				String strDate = htmlArticle.getFirstChild().getNextSibling().getFirstChild().getFirstChild().getNextSibling().asText();
				Date date = SDF.parse(strDate);
				String comment = htmlArticle.getFirstChild().getNextSibling().getFirstChild().getNextSibling().getFirstChild()
						.getFirstChild().getFirstChild().getFirstChild().asText();

				String language = detectLibreTranslateUseCase.detect(comment);
				Review review = Review.builder().comment(comment).name(name).date(date).rating(rating).title(title)
						.lastMetadataSync(new Date()).provider(PROVIDER).build();
				if (language != null && !language.equals(lang)) {
					foreignComments.add(review);
				}
				else {
					reviews.add(review);
				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		});

		if (reviews.size() < 10 && !CollectionUtils.isEmpty(foreignComments)) {
			for (Review review : foreignComments) {
				review.setComment(translateLibreTranslateUseCase.translate(review.getComment(), lang));
				reviews.add(review);
				if (reviews.size() == 10) {
					break;
				}
			}
		}

		webClient.close();

		return reviews;
	}

}
