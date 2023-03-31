package com.martinia.indigo.adapters.out.metadata;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlArticle;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.domain.model.inner.Review;
import com.martinia.indigo.domain.util.DataUtils;
import com.martinia.indigo.ports.out.metadata.GoodReadsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodReadsServiceImpl implements GoodReadsService {

	private static SimpleDateFormat SDF = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

	private String endpoint = "https://www.goodreads.com/";

	private String PROVIDER = "Goodreads";

	@Override
	public List<Review> getReviews(String title, List<String> authors) {

		List<Review> listReviews = null;
		try {
			String path = getPath(title, authors.stream().collect(Collectors.joining(" ")));
			if (path != null) {
				listReviews = getReviews(path, 1);
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

	private List<Review> getReviews(String url, int numPage) throws Exception {

		List<Review> reviews = new ArrayList<>();

		WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);

		HtmlPage page = webClient.getPage("https://www.goodreads.com" + url);

		page.getByXPath("//article[@class='ReviewCard']").stream().forEach(item -> {
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

				reviews.add(Review.builder().comment(comment).name(name).date(date).rating(rating).title(title).lastMetadataSync(new Date())
						.provider(PROVIDER).build());
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		});

		//		long next = page.getByXPath("//li[@class='a-last']").stream().count();
		//		long end = page.getByXPath("//li[@class='a-disabled a-last']").stream().count();

		webClient.close();

		//		if (next > 0 && end == 0) {
		//			reviews.addAll(getReviews(asin, ++numPage));
		//		}

		return reviews;
	}

	@Override
	public String[] findBook(String key, String title, List<String> authors, boolean withAuthor) {

		String[] ret = null;

		try {

			String author = String.join(" ", authors);

			author = StringUtils.stripAccents(author).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");
			title = StringUtils.stripAccents(title.replaceAll("ñ", "-")).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");

			String url = endpoint + "book/title.xml?title=" + title.replace(" ", "-") + "&key=" + key;
			if (withAuthor) {
				url += "&authors=" + author.replace(" ", "%20");
			}

			String xml = DataUtils.getData(url);

			if (StringUtils.isNoneEmpty(xml)) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

				if (doc.select("book").first() != null) {

					String finalTitle = title;
					String finalAuthor = author;
					ret = doc.select("book").stream().map(book -> {

						String name = book.select("title").text();
						String filterName = StringUtils.stripAccents(name).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
								.toLowerCase().trim();

						String[] terms = finalTitle.split(" ");

						long hasTerms = Arrays.stream(terms).filter(term -> {
							return (filterName.contains(StringUtils.stripAccents(term).toLowerCase().trim()));
						}).count();

						if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {

							String _author = book.select("authors").select("author").select("name").text();

							String filterAuthor = StringUtils.stripAccents(_author).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
									.toLowerCase().trim();

							terms = finalAuthor.split(" ");

							hasTerms = Arrays.stream(terms).filter(term -> {
								return (filterAuthor.contains(StringUtils.stripAccents(term).toLowerCase().trim()));
							}).count();

							if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {

								float rating = Float.parseFloat(book.select("average_rating").get(0).text());
								String similar = book.select("similar_books").select("book").stream().map(similarBook -> {
									String similar_title = similarBook.select("title").text();
									String similar_author = similarBook.select("authors").select("author").select("name").text();

									return similar_title + "@;@" + similar_author;
								}).collect(Collectors.joining("#;#"));

								if (StringUtils.isNoneEmpty(similar)) {
									return new String[] { String.valueOf(rating), similar, PROVIDER };
								}

							}

						}
						return null;
					}).filter(Objects::nonNull).findFirst().orElse(null);

				}
			}
		}
		catch (Exception e) {
			log.error(endpoint + "book/title.xml?title=" + title.replace(" ", "-") + "&key=" + key);
		}

		if (ret == null && !withAuthor) {
			ret = findBook(key, title, authors, true);
		}

		return ret;

	}

	@Override
	public String[] findAuthor(String key, String subject) {

		String[] ret = null;

		try {

			subject = StringUtils.stripAccents(subject).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");

			String url = endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key;
			String xml = DataUtils.getData(url);

			if (StringUtils.isNoneEmpty(xml)) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
				if (doc.select("author").first() != null) {
					String name = doc.select("author").select("name").get(0).text();
					String id = doc.select("author").select("id").get(0).text();

					if (name != null && id != null) {

						String filterName = StringUtils.stripAccents(name).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
								.toLowerCase().trim();

						String[] terms = subject.split(" ");

						long hasTerms = Arrays.stream(terms).filter(term -> {
							return (filterName.contains(StringUtils.stripAccents(term).toLowerCase().trim()));
						}).count();

						if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
							ret = getAuthorInfo(key, id);
						}
						else {
							log.debug("************************** " + subject + " is not contained in " + name);
						}

					}
				}
			}
		}
		catch (Exception e) {
			log.error(endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key);
		}

		return ret;

	}

	private String[] getAuthorInfo(String key, String id) {

		String[] ret = null;

		try {
			String url = endpoint + "author/show/" + id + "?format=xml&key=" + key;
			String xml = DataUtils.getData(url);

			if (xml != null) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
				if (doc.select("author").first() != null) {
					String name = doc.select("author").select("name").get(0).text();
					String description = doc.select("author").select("about").text();
					String image = doc.select("author").select("image_url").get(0).text();

					if (StringUtils.isNotEmpty(name)) {
						ret = new String[] { description, image, PROVIDER };
					}
				}
			}

		}
		catch (Exception e) {
			log.error(endpoint + "author/show/" + id + "?format=xml&key=" + key);
		}

		return ret;

	}

}
