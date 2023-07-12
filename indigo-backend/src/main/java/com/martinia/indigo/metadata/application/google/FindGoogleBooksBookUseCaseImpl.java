package com.martinia.indigo.metadata.application.google;

import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.usecases.google.FindGoogleBooksBookUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@ConditionalOnProperty(name = "flags.google", havingValue = "true")
public class FindGoogleBooksBookUseCaseImpl implements FindGoogleBooksBookUseCase {

	@Value("${metadata.google.url}")
	private String endpoint;

	@Resource
	private DataUtils dataUtils;

	@Override
	public String[] findBook(String title, List<String> authors) {

		String[] ret = null;

		try {

			String author = String.join(" ", authors);

			author = StringUtils.stripAccents(author).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");
			title = StringUtils.stripAccents(title.replaceAll("ñ", "-")).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
					.replaceAll(" ", "%20");

			String json = dataUtils.getData(endpoint.replace("$title", title));

			if (StringUtils.isNoneEmpty(json)) {
				JsonParser springParser = JsonParserFactory.getJsonParser();
				Map<String, Object> map = springParser.parseMap(json);

				if (map.containsKey("items")) {
					ArrayList<LinkedHashMap<String, Object>> items = (ArrayList<LinkedHashMap<String, Object>>) map.get("items");

					String finalTitle = title;
					String finalAuthor = author;
					ret = items.stream().map(item -> {
						LinkedHashMap<String, Object> volumeInfo = (LinkedHashMap<String, Object>) item.get("volumeInfo");

						String name = volumeInfo.get("title").toString();
						String filterName = StringUtils.stripAccents(name).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
								.toLowerCase().trim();

						String[] terms = finalTitle.split("%20");

						long hasTerms = Arrays.stream(terms)
								.filter(term -> filterName.contains(StringUtils.stripAccents(term).toLowerCase().trim())).count();

						if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {

							ArrayList<String> _authors = (ArrayList<String>) volumeInfo.get("authors");
							if (_authors != null) {
								return _authors.stream().map(_author -> {

									String filterAuthor = StringUtils.stripAccents(_author).replaceAll("[^a-zA-Z0-9]", " ")
											.replaceAll("\\s+", " ").toLowerCase().trim();

									String[] authorTerms = finalAuthor.split(" ");

									long authorHasTerms = Arrays.stream(authorTerms)
											.filter(term -> filterAuthor.contains(StringUtils.stripAccents(term).toLowerCase().trim()))
											.count();

									if (authorTerms.length == 1 && authorHasTerms > 0 || authorTerms.length > 1 && authorHasTerms > 1) {

										if (volumeInfo.get("averageRating") != null) {
											String rating = volumeInfo.get("averageRating").toString();
											return new String[] { rating, ProviderEnum.GOOGLE.name() };
										}
									}
									return null;
								}).filter(Objects::nonNull).findFirst().orElse(null);
							}

						}
						return null;

					}).filter(Objects::nonNull).findFirst().orElse(null);
				}

			}

		}
		catch (Exception e) {
			log.error(e.getMessage());
		}

		return ret;
	}
}
