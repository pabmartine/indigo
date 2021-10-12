package com.martinia.indigo.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.model.indigo.MyAuthor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WikipediaComponent {

	private String PROVIDER = "Wikipedia";

	public MyAuthor findAuthor(String subject, String lang) {

		MyAuthor customAuthor = null;

		subject = StringUtils.stripAccents(subject)
				.replaceAll("[^a-zA-Z0-9]", " ")
				.replaceAll("\\s+", " ");

		String url = "https://" + lang
				+ ".wikipedia.org/w/api.php?action=query&format=json&list=search&utf8=1&origin=*&srsearch="
				+ subject.replace(" ", "%20");
		try {

			String json = DataUtils.getData(url);

			if (json != null) {

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

				JsonNode jsonNodeRoot = objectMapper.readTree(json);
				JsonNode query = jsonNodeRoot.get("query");
				JsonNode search = query.get("search");

				String strTitle = null;
				String[] terms = subject.split(" ");
				if (search.isArray()) {
					for (final JsonNode objNode : search) {
						JsonNode title = objNode.get("title");
						strTitle = title.asText();

						String filterTitle = StringUtils.stripAccents(strTitle)
								.replaceAll("[^a-zA-Z0-9]", " ")
								.replaceAll("\\s+", " ")
								.toLowerCase()
								.trim();

						boolean contains = true;
						for (String term : terms) {
							term = StringUtils.stripAccents(term)
									.toLowerCase()
									.trim();
							if (!filterTitle.contains(term)) {
								contains = false;
							}
						}

						if (contains)
							break;
						else
							strTitle = null;

					}
				}

				if (StringUtils.isNotEmpty(strTitle)) {
					customAuthor = getAuthorInfo(strTitle, lang);
				}
			}
		} catch (Exception e) {
			log.error(url);
			e.printStackTrace();
		}

		return customAuthor;
	}

	private MyAuthor getAuthorInfo(String subject, String lang) {

		MyAuthor customAuthor = null;

		subject = StringUtils.stripAccents(subject)
				.replaceAll("[^a-zA-Z0-9]", " ")
				.replaceAll("\\s+", " ");

		String url = "https://" + lang
				+ ".wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro&explaintext&generator=search&gsrlimit=1&redirects=1&piprop=original&gsrsearch="
				+ subject.replace(" ", "%20");
		try {
			String json = DataUtils.getData(url);

			if (json != null) {

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

				JsonNode jsonNodeRoot = objectMapper.readTree(json);
				JsonNode query = jsonNodeRoot.get("query");
				if (query != null) {

					JsonNode search = query.get("pages");
					JsonNode title = search.findPath("title");
					JsonNode extract = search.findPath("extract");
					JsonNode source = search.findPath("original")
							.get("source");

					String strTitle = title.asText();
					String filterTitle = StringUtils.stripAccents(strTitle)
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

						if (!filterTitle.contains(term)) {
							contains = false;
						}
					}

					if (extract.asText()
							.toLowerCase()
							.startsWith("for the")) {
						log.info(title + " --> " + extract.asText());
					}

					if (contains) {
						customAuthor = new MyAuthor(title.asText(), extract.asText(),
								source != null ? source.asText() : null, PROVIDER);
					}
				}
			}
		} catch (Exception e) {
			log.error(url);

			e.printStackTrace();
		}

		return customAuthor;
	}

}
