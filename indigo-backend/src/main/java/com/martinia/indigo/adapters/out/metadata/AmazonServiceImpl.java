package com.martinia.indigo.adapters.out.metadata;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.domain.model.inner.Review;
import com.martinia.indigo.ports.out.metadata.AmazonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AmazonServiceImpl implements AmazonService {
	@Override
	public List<Review> getReviews(String title, List<String> authors) {
		List<Review> listReviews = null;
		try {
			String asin = getAsin(title, authors.stream().collect(Collectors.joining(" ")));
			if (asin != null) {
				listReviews = getReviews(asin, 1);
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
		return listReviews;
	}

	private String getAsin(String title, String author) throws Exception {

		AtomicReference<String> asin = new AtomicReference<>();

		WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);

		String tokenized_title = normalize(title);
		String tokenized_author = normalize(author);

		String url = "https://www.amazon.es/s?k=" + tokenized_title + "+" + tokenized_author + "&i=stripbooks";
		HtmlPage page = webClient.getPage(url);

		page.getByXPath("//div[@data-component-type='s-search-result']").stream().forEach(item -> {
			HtmlDivision htmlDivision = (HtmlDivision) item;
			try {
				String compareAsin = ((HtmlDivision) page.getByXPath("//div[@data-component-type='s-search-result']").get(0)).getAttribute(
						"data-asin");
				String compareTitle = ((HtmlDivision) page.getByXPath("//div[@data-component-type='s-search-result']").get(0))
						.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getFirstChild().getFirstChild().getNextSibling()
						.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getFirstChild().asText();
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

				if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
					asin.set(compareAsin);
				}
			}
			catch (Exception e) {
				log.error(e.getMessage());
			}
		});

		webClient.close();

		return asin.get();
	}

	private static String normalize(String title) {
		if (title.contains("(")) {
			title = title.substring(0, title.indexOf("(")) + title.substring(title.indexOf(")") + 1, title.length());
		}
		return Normalizer.normalize(title, Normalizer.Form.NFD).toLowerCase().replaceAll("[^\\p{ASCII}]", "")
				.replaceAll(" ", "+")
				.replaceAll(",", "")
				.replaceAll("\\.", "+").replaceAll(":", "+").replaceAll("\\+\\+", "+");
	}

	private List<Review> getReviews(String asin, int numPage) throws Exception {

		List<Review> reviews = new ArrayList<>();

		WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);

		String url = "http://www.amazon.es/product-reviews/" + asin + "/?showViewpoints=0&sortBy=byRankDescending&pageNumber=" + numPage;
		HtmlPage page = webClient.getPage(url);

		page.getByXPath("//div[@class='a-section review aok-relative']").stream().forEach(item -> {
			HtmlDivision htmlDivision = (HtmlDivision) item;
			try {
				String name = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getFirstChild().getNextSibling()
						.getFirstChild().asText();
				String rating = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild()
						.getFirstChild().asText().substring(0, 3);
				String title = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild()
						.getNextSibling().getNextSibling().getFirstChild().getNextSibling().getFirstChild().asText();
				String date = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getNextSibling().getFirstChild()
						.asText();
				Matcher matcher = Pattern.compile("\\d+").matcher(date);
				matcher.find();
				int value = Integer.valueOf(matcher.group());
				date = date.substring(date.indexOf(String.valueOf(value)), date.length());
				String comment = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getNextSibling()
						.getNextSibling().getNextSibling().getFirstChild().getFirstChild().getNextSibling().getFirstChild().asText();

				reviews.add(Review.builder().comment(comment).name(name).date(date).rating(rating).title(title).build());
			}
			catch (Exception e) {
				log.error(e.getMessage());
			}
		});

		long next = page.getByXPath("//li[@class='a-last']").stream().count();
		long end = page.getByXPath("//li[@class='a-disabled a-last']").stream().count();

		webClient.close();

//		if (next > 0 && end == 0) {
//			reviews.addAll(getReviews(asin, ++numPage));
//		}

		return reviews;
	}
}