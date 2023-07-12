package com.martinia.indigo.metadata.application.amazon;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.common.model.Review;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.usecases.amazon.FindAmazonReviewsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(name = "flags.amazon", havingValue = "true")
public class FindAmazonReviewsUseCaseImpl implements FindAmazonReviewsUseCase {

	private static SimpleDateFormat SDF = new SimpleDateFormat("d MMMM yyyy");

	@Value("${metadata.amazon.asin}")
	private String endpointAsin;

	@Value("${metadata.amazon.reviews}")
	private String endpointReviews;

	@Override
	public List<Review> getReviews(String title, List<String> authors) {

		try {
			InetAddress localhost = InetAddress.getLocalHost();
			if (!localhost.getHostAddress().equals("127.0.1.1")) {
				log.error("Tryed to obtain reviews from Amazon from a docker container.");
				return null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

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

		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);

		String tokenized_title = normalize(title);
		String tokenized_author = normalize(author);

		HtmlPage page = webClient.getPage(endpointAsin.replace("$title", tokenized_title).replace("$author", tokenized_author));

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
		return Normalizer.normalize(title, Normalizer.Form.NFD).toLowerCase().replaceAll("[^\\p{ASCII}]", "").replaceAll(" ", "+")
				.replaceAll(",", "").replaceAll("-", " ").replaceAll("\\.", "+").replaceAll(":", "+").replaceAll("\\+\\+", "+");
	}

	private List<Review> getReviews(String asin, int numPage) throws Exception {

		List<Review> reviews = new ArrayList<>();

		WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);

		String url = endpointReviews.replace("$asin", asin).replace("$numPage", String.valueOf(numPage));
		HtmlPage page = webClient.getPage(url);

		page.getByXPath("//div[@class='a-section review aok-relative']").stream().forEach(item -> {
			HtmlDivision htmlDivision = (HtmlDivision) item;
			try {
				String name = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getFirstChild().getNextSibling()
						.getFirstChild().asText();
				int rating = Integer.valueOf(
						htmlDivision.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild().getFirstChild()
								.asText().substring(0, 1));
				String title = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild()
						.getNextSibling().getNextSibling().getFirstChild().getNextSibling().getFirstChild().asText();
				String strDate = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getNextSibling()
						.getFirstChild().asText();
				Matcher matcher = Pattern.compile("\\d+").matcher(strDate);
				matcher.find();
				int value = Integer.valueOf(matcher.group());
				strDate = strDate.substring(strDate.indexOf(String.valueOf(value)), strDate.length()).replaceAll("de ", "");
				Date date = SDF.parse(strDate);
				String comment = htmlDivision.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getNextSibling()
						.getNextSibling().getNextSibling().getFirstChild().getFirstChild().getNextSibling().getFirstChild().asText();

				reviews.add(Review.builder().comment(comment).name(name).date(date).rating(rating).title(title).lastMetadataSync(new Date())
						.provider(ProviderEnum.WIKIPEDIA.name()).build());
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
