package com.martinia.indigo.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.model.indigo.MyAuthor;
import com.martinia.indigo.model.indigo.MyBook;
import com.martinia.indigo.repository.calibre.BookRepository;
import com.martinia.indigo.repository.indigo.ConfigurationRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GoodReadsComponent {

	@Autowired
	ConfigurationRepository configurationRepository;

	@Autowired
	private BookRepository bookRepository;

	private String endpoint = "https://www.goodreads.com/";
	private String PROVIDER = "Goodreads";

	public static void main(String[] args) {
		GoodReadsComponent g = new GoodReadsComponent();
//		g.findSimilarBooks("Sereno en el peligro", "Lorenzo Silva");
	}

	public MyBook findBook(String title, String author) {

		MyBook customBook = null;

		String key = configurationRepository.findById("goodreads.key")
				.get()
				.getValue();

		try {

			author = StringUtils.stripAccents(author)
					.replaceAll("[^a-zA-Z0-9]", " ")
					.replaceAll("\\s+", " ");
			title = StringUtils.stripAccents(title.replaceAll("ñ", "-"))
					.replaceAll("[^a-zA-Z0-9]", " ")
					.replaceAll("\\s+", " ");

			String url = endpoint + "book/title.xml?title=" + title.replace(" ", "-") + "&key=" + key;
			String xml = DataUtils.getData(url);

			if (xml != null) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
				if (doc.select("book")
						.first() != null) {
					for (int i = 0; i < doc.select("book")
							.size(); i++) {

						String name = doc.select("book")
								.select("title")
								.get(i)
								.text();
						String filterName = StringUtils.stripAccents(name)
								.replaceAll("[^a-zA-Z0-9]", " ")
								.replaceAll("\\s+", " ")
								.toLowerCase()
								.trim();

						String[] terms = title.split(" ");
						boolean contains = true;
						for (String term : terms) {

							term = StringUtils.stripAccents(term)
									.toLowerCase()
									.trim();

							if (!filterName.contains(term)) {
								contains = false;
							}
						}

						if (contains) {

							String _author = doc.select("book")
									.select("authors")
									.select("author")
									.select("name")
									.get(i)
									.text();

							String filterAuthor = StringUtils.stripAccents(_author)
									.replaceAll("[^a-zA-Z0-9]", " ")
									.replaceAll("\\s+", " ")
									.toLowerCase()
									.trim();

							terms = author.split(" ");

							contains = true;
							for (String term : terms) {

								term = StringUtils.stripAccents(term)
										.toLowerCase()
										.trim();

								if (!filterAuthor.contains(term)) {
									contains = false;
								}
							}

							if (contains) {
								float rating = Float.parseFloat(doc.select("book")
										.select("average_rating")
										.get(0)
										.text());
								String similar = "";
								for (int j = 0; j < doc.select("book")
										.select("similar_books")
										.select("book")
										.size(); j++) {

									String similar_title = doc.select("book")
											.select("similar_books")
											.select("book")
											.select("title")
											.get(j)
											.text();
									String similar_author = doc.select("book")
											.select("similar_books")
											.select("book")
											.select("authors")
											.select("author")
											.select("name")
											.get(j)
											.text();

									List<Book> books = bookRepository.findByTitle(similar_title);
									if (books != null && books.size() > 0) {
										for (Book book : books) {
											String authors = book.getAuthorSort();

											String filterSimilarAuthor = StringUtils.stripAccents(similar_author)
													.replaceAll("[^a-zA-Z0-9]", " ")
													.replaceAll("\\s+", " ")
													.toLowerCase()
													.trim();

											String[] similarTerms = StringUtils.stripAccents(authors)
													.replaceAll("[^a-zA-Z0-9]", " ")
													.replaceAll("\\s+", " ")
													.split(" ");

											boolean similarContains = true;
											for (String similarTerm : similarTerms) {
												similarTerm = StringUtils.stripAccents(similarTerm)
														.toLowerCase()
														.trim();
												if (!filterSimilarAuthor.contains(similarTerm)) {
													similarContains = false;
												}
											}

											if (similarContains) {
												similar += book.getId() + ";";
											}
										}
									}

								}

								customBook = new MyBook(rating,
										similar.length() == 0 ? null
												: similar.substring(0, similar.length() - 1)
														.trim(),
										PROVIDER);
								break;

							}

						}
					}
				}
			}
		} catch (Exception e) {
			log.error(endpoint + "book/title.xml?title=" + title.replace(" ", "-") + "&key=" + key);
			e.printStackTrace();
		}

		return customBook;

	}

	public MyAuthor findAuthor(String subject) {

		MyAuthor customAuthor = null;

		String key = configurationRepository.findById("goodreads.key")
				.get()
				.getValue();

		try {

			subject = StringUtils.stripAccents(subject)
					.replaceAll("[^a-zA-Z0-9]", " ")
					.replaceAll("\\s+", " ");

			String url = endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key;
			String xml = DataUtils.getData(url);

			if (xml != null) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
				if (doc.select("author")
						.first() != null) {
					String name = doc.select("author")
							.select("name")
							.get(0)
							.text();
					String id = doc.select("author")
							.select("id")
							.get(0)
							.text();

					if (name != null && id != null) {

						String filterName = StringUtils.stripAccents(name)
								.replaceAll("[^a-zA-Z0-9]", " ")
								.replaceAll("\\s+", " ")
								.toLowerCase()
								.trim();

						String[] terms = subject.split(" ");
						boolean contains = true;
						for (String term : terms) {

							term = StringUtils.stripAccents(term)
									.toLowerCase()
									.trim();

							if (!filterName.contains(term)) {
								contains = false;
							}
						}

						if (contains)
							customAuthor = getAuthorInfo(id);
						else
							log.info("************************** " + subject + " is not contained in " + name);

					}
				}
			}
		} catch (Exception e) {
			log.error(endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key);
			e.printStackTrace();
		}

		return customAuthor;

	}

	private MyAuthor getAuthorInfo(String id) {

		MyAuthor customAuthor = null;

		String key = configurationRepository.findById("goodreads.key")
				.get()
				.getValue();

		try {
			String url = endpoint + "author/show/" + id + "?format=xml&key=" + key;
			String xml = DataUtils.getData(url);

			if (xml != null) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
				if (doc.select("author")
						.first() != null) {
					String name = doc.select("author")
							.select("name")
							.get(0)
							.text();
					String description = doc.select("author")
							.select("about")
							.text();
					String image = doc.select("author")
							.select("image_url")
							.get(0)
							.text();

					if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(description)) {
						customAuthor = new MyAuthor(name, description, image, PROVIDER);
					}
				}
			}
		} catch (Exception e) {
			log.error(endpoint + "author/show/" + id + "?format=xml&key=" + key);
			e.printStackTrace();
		}

		return customAuthor;

	}

}
