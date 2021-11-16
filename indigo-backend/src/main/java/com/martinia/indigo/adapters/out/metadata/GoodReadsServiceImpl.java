package com.martinia.indigo.adapters.out.metadata;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.util.DataUtils;
import com.martinia.indigo.ports.out.metadata.GoodReadsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GoodReadsServiceImpl implements GoodReadsService {

	private String endpoint = "https://www.goodreads.com/";
	private String PROVIDER = "Goodreads";

	@Override
	public String[] findBook(String key, List<Book> list, String title, List<String> authors, boolean withAuthor) {

		String[] ret = null;

		try {

			String author = String.join(" ", authors);

			author = StringUtils.stripAccents(author)
					.replaceAll("[^a-zA-Z0-9]", " ")
					.replaceAll("\\s+", " ");
			title = StringUtils.stripAccents(title.replaceAll("ñ", "-"))
					.replaceAll("[^a-zA-Z0-9]", " ")
					.replaceAll("\\s+", " ");

			String url = endpoint + "book/title.xml?title=" + title.replace(" ", "-") + "&key=" + key;
			if (withAuthor) {
				url += "&author=" + author.replace(" ", "%20");
			}

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
						boolean contains = false;
						for (String term : terms) {

							term = StringUtils.stripAccents(term)
									.toLowerCase()
									.trim();

							if (!filterName.contains(term)) {
								contains = true;
								break;
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

									List<Book> books = list.stream()
											.filter(b -> b.getTitle()
													.equalsIgnoreCase(similar_title))
											.collect(Collectors.toList());
									if (books != null && books.size() > 0) {
										for (Book book : books) {
											String _authors = String.join(" ", book.getAuthors());

											String filterSimilarAuthor = StringUtils.stripAccents(similar_author)
													.replaceAll("[^a-zA-Z0-9]", " ")
													.replaceAll("\\s+", " ")
													.toLowerCase()
													.trim();

											String[] similarTerms = StringUtils.stripAccents(_authors)
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

								ret = new String[] { String.valueOf(rating),
										similar.length() == 0 ? null
												: similar.substring(0, similar.length() - 1)
														.trim(),
										PROVIDER };
								break;

							} else if (!withAuthor) {
								ret = findBook(key, list, title, authors, true);
							}

						}
					}
				}
			}
		} catch (Exception e) {
			log.error(endpoint + "book/title.xml?title=" + title.replace(" ", "-") + "&key=" + key);
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public String[] findAuthor(String key, String subject) {

		String[] ret = null;

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
						boolean contains = false;
						for (String term : terms) {

							term = StringUtils.stripAccents(term)
									.toLowerCase()
									.trim();

							if (!filterName.contains(term)) {
								contains = true;
								break;
							}
						}

						if (contains)
							ret = getAuthorInfo(key, id);
						else
							log.debug("************************** " + subject + " is not contained in " + name);

					}
				}
			}
		} catch (Exception e) {
			log.error(endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key);
			e.printStackTrace();
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

					if (StringUtils.isNotEmpty(name)) {
						ret = new String[] { description, image, PROVIDER };
					}
				}
			}

		} catch (Exception e) {
			log.error(endpoint + "author/show/" + id + "?format=xml&key=" + key);
			e.printStackTrace();
		}

		return ret;

	}

}
