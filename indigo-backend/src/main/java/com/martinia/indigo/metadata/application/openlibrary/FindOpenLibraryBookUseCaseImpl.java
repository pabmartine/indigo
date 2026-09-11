package com.martinia.indigo.metadata.application.openlibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;
import com.martinia.indigo.metadata.domain.ports.usecases.openlibrary.FindOpenLibraryBookUseCase;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "flags.openlibrary", havingValue = "true")
public class FindOpenLibraryBookUseCaseImpl implements FindOpenLibraryBookUseCase {

	@Value("${metadata.openlibrary.books}")
	private String endpoint;
	@Value("${metadata.openlibrary.books-by-isbn}")
	private String isbnEndpoint;

	@Resource
	private DataUtils dataUtils;

	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private LocalOpenLibraryLookup localLookup;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public BookMetadataResult findBook(final String title, final List<String> authors) {
		return findBook(BookMetadataQuery.builder().title(title).authors(authors).build());
	}

	@Override
	public BookMetadataResult findBook(final BookMetadataQuery query) {
		if (localLookup != null) {
			try {
				var local = localLookup.find(query);
				if (local != null) return local;
			} catch (RuntimeException exception) {
				log.warn("Local Open Library index unavailable; falling back to HTTP", exception);
			}
		}
		final String isbn = query.preferredIsbn();
		if (StringUtils.isNotBlank(isbn)) {
			final BookMetadataResult isbnResult = findByIsbn(isbn);
			if (isbnResult != null) {
				return isbnResult;
			}
		}
		return findByTitleAndAuthor(query.getTitle(), query.getAuthors());
	}

	private BookMetadataResult findByIsbn(final String isbn) {
		try {
			final String json = dataUtils.getData(isbnEndpoint.replace("$isbn", encode(isbn)));
			if (StringUtils.isBlank(json)) {
				return null;
			}
			for (JsonNode document : objectMapper.readTree(json).path("docs")) {
				if (document.hasNonNull("key") || document.path("edition_key").isArray()) {
					return toResult(document, 1D);
				}
			}
			return null;
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not obtain Open Library metadata for ISBN " + isbn, exception);
		}
	}

	private BookMetadataResult findByTitleAndAuthor(final String title, final List<String> authors) {
		if (StringUtils.isBlank(title)) {
			return null;
		}

		try {
			final String author = String.join(" ", authors == null ? Collections.emptyList() : authors);
			final String url = endpoint
					.replace("$title", encode(title))
					.replace("$author", encode(author));
			final String json = dataUtils.getData(url);
			if (StringUtils.isBlank(json)) {
				return null;
			}

			for (JsonNode document : objectMapper.readTree(json).path("docs")) {
				if (matches(document, title, authors) && document.hasNonNull("ratings_average")) {
					return toResult(document, 1D);
				}
			}
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not obtain Open Library metadata for " + title, exception);
		}
		return null;
	}

	private BookMetadataResult toResult(final JsonNode document, final double confidence) {
		return BookMetadataResult.builder()
				.ratingAverage(document.hasNonNull("ratings_average")
						? (float) document.path("ratings_average").asDouble()
						: null)
				.ratingsCount(document.hasNonNull("ratings_count") ? document.path("ratings_count").asLong() : null)
				.provider(ProviderEnum.OPEN_LIBRARY.name())
				.openLibraryWorkId(normalizeOpenLibraryId(document.path("key").asText(null)))
				.openLibraryEditionId(firstValue(document.path("edition_key")))
				.matchConfidence(confidence)
				.build();
	}

	private String firstValue(final JsonNode values) {
		return values.isArray() && !values.isEmpty() ? values.get(0).asText(null) : null;
	}

	private String normalizeOpenLibraryId(final String value) {
		if (value == null) {
			return null;
		}
		final int separator = value.lastIndexOf('/');
		return separator >= 0 ? value.substring(separator + 1) : value;
	}

	private boolean matches(final JsonNode document, final String title, final List<String> authors) {
		final String expectedTitle = normalize(title);
		final String actualTitle = normalize(document.path("title").asText());
		if (!actualTitle.equals(expectedTitle) && !actualTitle.startsWith(expectedTitle + " ")) {
			return false;
		}

		if (authors == null || authors.isEmpty()) {
			return true;
		}
		for (String author : authors) {
			final String expectedAuthor = normalize(author);
			for (JsonNode candidate : document.path("author_name")) {
				final String actualAuthor = normalize(candidate.asText());
				if (actualAuthor.equals(expectedAuthor)) {
					return true;
				}
			}
		}
		return false;
	}

	private String encode(final String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private String normalize(final String value) {
		return StringUtils.stripAccents(StringUtils.defaultString(value))
				.replaceAll("[^a-zA-Z0-9]", " ")
				.replaceAll("\\s+", " ")
				.toLowerCase()
				.trim();
	}
}
