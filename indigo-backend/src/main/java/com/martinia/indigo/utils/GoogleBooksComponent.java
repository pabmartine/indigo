package com.martinia.indigo.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.MyBook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GoogleBooksComponent {

	private String endpoint = "https://www.googleapis.com/books/v1/volumes?q=";
	private String PROVIDER = "Google Books";

	public static void main(String[] args) {
		GoogleBooksComponent g = new GoogleBooksComponent();
		g.findBook("Sidi", "Perez Reverte");
	}

	public MyBook findBook(String title, String author) {

		MyBook customBook = null;

		try {

			author = StringUtils.stripAccents(author)
					.replaceAll("[^a-zA-Z0-9]", " ")
					.replaceAll("\\s+", " ");
			title = StringUtils.stripAccents(title.replaceAll("ñ", "-"))
					.replaceAll("[^a-zA-Z0-9]", " ")
					.replaceAll("\\s+", " ")
					.replaceAll(" ", "%20");

			String url = endpoint + "intitle:" + title;
			String json = DataUtils.getData(url);

			if (json != null) {
				JsonParser springParser = JsonParserFactory.getJsonParser();
				Map<String, Object> map = springParser.parseMap(json);

				if (map.containsKey("items")) {
					ArrayList<LinkedHashMap<String, Object>> items = (ArrayList<LinkedHashMap<String, Object>>) map
							.get("items");
					for (LinkedHashMap<String, Object> item : items) {
						LinkedHashMap<String, Object> volumeInfo = (LinkedHashMap<String, Object>) item
								.get("volumeInfo");

						String name = volumeInfo.get("title")
								.toString();
						String filterName = StringUtils.stripAccents(name)
								.replaceAll("[^a-zA-Z0-9]", " ")
								.replaceAll("\\s+", " ")
								.toLowerCase()
								.trim();

						String[] terms = title.split("%20");
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
							ArrayList<String> _authors = (ArrayList<String>) volumeInfo.get("authors");
							if (_authors != null)
								for (String _author : _authors) {

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
										if (volumeInfo.get("averageRating") != null) {
											float rating = Float.parseFloat(volumeInfo.get("averageRating")
													.toString());
											customBook = new MyBook(rating, null, PROVIDER);
											break;
										}
									}
								}

							if (customBook != null) {
								break;
							}
						}
					}
				}

			}

		} catch (

		Exception e) {
			log.error(endpoint + "intitle:" + title);
			e.printStackTrace();
		}

		return customBook;
	}
}
