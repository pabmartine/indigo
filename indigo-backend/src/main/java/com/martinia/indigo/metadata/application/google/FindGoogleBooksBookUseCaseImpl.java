package com.martinia.indigo.metadata.application.google;

import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.ports.usecases.google.FindGoogleBooksBookUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
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

	@Value("${metadata.google.api-key:}")
	private String apiKey;

	@Resource
	private DataUtils dataUtils;

	@Override
	public BookMetadataResult findBook(String title, List<String> authors) {

		BookMetadataResult ret = null;
		final String originalTitle = title;

		try {

			List<String> safeAuthors = authors == null ? List.of() : authors;
			List<String> normalizedAuthors = safeAuthors.stream().map(this::normalize).toList();
			String author = String.join(" ", safeAuthors);

			author = normalize(author);
			title = StringUtils.stripAccents(title).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
					.replaceAll(" ", "%20");

			String requestUrl = endpoint.replace("$title", title).replace("$author", author.replace(" ", "%20"));
			if (StringUtils.isNotBlank(apiKey)) {
				requestUrl += (requestUrl.contains("?") ? "&" : "?") + "key=" + apiKey.trim();
			}
			String json = dataUtils.getData(requestUrl);

			if (StringUtils.isNoneEmpty(json)) {
				JsonParser springParser = JsonParserFactory.getJsonParser();
				Map<String, Object> map = springParser.parseMap(json);

				if (map.containsKey("items")) {
					ArrayList<LinkedHashMap<String, Object>> items = (ArrayList<LinkedHashMap<String, Object>>) map.get("items");

					String finalTitle = title;
					ret = items.stream().map(item -> {
						LinkedHashMap<String, Object> volumeInfo = (LinkedHashMap<String, Object>) item.get("volumeInfo");

						String name = volumeInfo.get("title").toString();
						String filterName = StringUtils.stripAccents(name).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
								.toLowerCase().trim();

						String expectedTitle = finalTitle.replace("%20", " ").toLowerCase().trim();

						if (filterName.equals(expectedTitle) || filterName.startsWith(expectedTitle + " ")) {

							ArrayList<String> _authors = (ArrayList<String>) volumeInfo.get("authors");
							if (_authors != null && !normalizedAuthors.isEmpty()) {
								return _authors.stream().map(_author -> {

									String filterAuthor = StringUtils.stripAccents(_author).replaceAll("[^a-zA-Z0-9]", " ")
											.replaceAll("\\s+", " ").toLowerCase().trim();

									if (normalizedAuthors.contains(filterAuthor)) {

										if (volumeInfo.get("averageRating") != null) {
											return BookMetadataResult.builder()
													.ratingAverage(Float.valueOf(volumeInfo.get("averageRating").toString()))
													.ratingsCount(asLong(volumeInfo.get("ratingsCount")))
													.provider(ProviderEnum.GOOGLE.name())
													.matchConfidence(1D)
													.build();
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
			throw new IllegalStateException("Could not obtain Google Books metadata for " + originalTitle, e);
		}

		return ret;
	}

	private Long asLong(final Object value) {
		return value == null ? null : Long.valueOf(value.toString());
	}

	private String normalize(final String value) {
		return StringUtils.stripAccents(StringUtils.defaultString(value))
				.replaceAll("[^a-zA-Z0-9]", " ")
				.replaceAll("\\s+", " ")
				.toLowerCase()
				.trim();
	}
}
