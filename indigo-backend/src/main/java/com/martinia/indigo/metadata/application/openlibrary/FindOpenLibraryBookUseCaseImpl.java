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
                com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
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
            BookMetadataResult identity = null;
            for (JsonNode document : documents(json)) {
                BookMetadataResult result = toResult(document, 1D);
                if (result.getRatingAverage() != null) return result;
                if (identity == null && (result.getOpenLibraryWorkId() != null || result.getOpenLibraryEditionId() != null)) {
                    identity = result;
                }
            }
            return identity;
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
            final String author = authors == null ? "" : authors.stream().filter(StringUtils::isNotBlank).findFirst().orElse("");
			final String url = editionSearchEndpoint()
					.replace("$title", encode(searchLiteral(title)))
					.replace("$author", encode(author));
			final String json = dataUtils.getData(url);
			if (StringUtils.isBlank(json)) {
				return null;
			}

            BookMetadataResult identity = null;
            for (JsonNode document : documents(json)) {
                if (!matches(document, title, authors)) continue;
                BookMetadataResult result = toResult(document, 1D);
                JsonNode edition = matchingEdition(document, title);
                result = BookMetadataResult.builder().provider(result.getProvider()).ratingAverage(result.getRatingAverage())
                        .ratingsCount(result.getRatingsCount()).openLibraryWorkId(result.getOpenLibraryWorkId())
                        .openLibraryEditionId(edition == null ? null : normalizeOpenLibraryId(edition.path("key").asText(null)))
                        .matchConfidence(result.getMatchConfidence()).build();
                if (result.getRatingAverage() != null) return result;
                if (identity == null && (result.getOpenLibraryWorkId() != null || result.getOpenLibraryEditionId() != null)) {
                    identity = result;
                }
            }
            return identity;
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not obtain Open Library metadata for " + title, exception);
		}
	}

    private JsonNode documents(String json) throws java.io.IOException {
        JsonNode response = objectMapper.readTree(json);
        if (!response.isObject() || response.has("error") || !response.path("docs").isArray()) {
            throw new IllegalStateException("Invalid Open Library response");
        }
        return response.path("docs");
    }

	private BookMetadataResult toResult(final JsonNode document, final double confidence) {
        Float average = null;
        if (document.hasNonNull("ratings_average")) {
            try {
                float value = Float.parseFloat(document.get("ratings_average").asText());
                if (Float.isFinite(value) && value >= 0 && value <= 5) average = value;
            } catch (NumberFormatException ignored) { /* Invalid rating does not invalidate the identity. */ }
        }
		return BookMetadataResult.builder()
				.ratingAverage(average)
				.ratingsCount(validCount(document.path("ratings_count")))
				.provider(ProviderEnum.OPEN_LIBRARY.name())
				.openLibraryWorkId(normalizeOpenLibraryId(document.path("key").asText(null)))
				.openLibraryEditionId(firstValue(document.path("edition_key")))
				.matchConfidence(confidence)
				.build();
	}

    private Long validCount(JsonNode value) {
        try {
            long count = Long.parseLong(value.asText());
            return count >= 0 ? count : null;
        } catch (NumberFormatException exception) { return null; }
    }

	private String firstValue(final JsonNode values) {
        if (values.isArray()) for (JsonNode value : values) {
            if (value.isTextual() && StringUtils.isNotBlank(value.asText())) return normalizeOpenLibraryId(value.asText());
        }
        return null;
	}

	private String normalizeOpenLibraryId(final String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		final int separator = value.lastIndexOf('/');
		return StringUtils.trimToNull(separator >= 0 ? value.substring(separator + 1) : value);
	}

	private boolean matches(final JsonNode document, final String title, final List<String> authors) {
		if (!com.martinia.indigo.metadata.application.BookMetadataTitles.matches(title,
                document.path("title").asText(null), document.path("subtitle").asText(null))
                && matchingEdition(document, title) == null) {
			return false;
		}

		if (authors == null || authors.isEmpty()) {
			return true;
		}
		for (String author : authors) {
			final String expectedAuthor = normalize(author);
			for (JsonNode candidate : document.path("author_name")) {
				final String actualAuthor = normalize(candidate.asText());
				if (!expectedAuthor.isBlank() && actualAuthor.equals(expectedAuthor)) {
					return true;
				}
			}
		}
		return false;
	}

    private String searchLiteral(String title) {
        return "\"" + title.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String editionSearchEndpoint() {
        // title= searches the work title only; q= also searches translated edition titles.
        // Upgrade the former default even when it is supplied through an environment variable.
        String url = endpoint.replace("?title=$title", "?q=$title").replace("&title=$title", "&q=$title");
        String fields = "key,title,subtitle,author_name,edition_key,ratings_average,ratings_count,editions,editions.key,editions.title,editions.subtitle";
        if (url.matches(".*[?&]fields=[^&]*.*")) return url.replaceFirst("([?&])fields=[^&]*", "$1fields=" + fields);
        return url + (url.contains("?") ? "&" : "?") + "fields=" + fields;
    }

    private JsonNode matchingEdition(JsonNode document, String title) {
        for (JsonNode edition : document.path("editions").path("docs")) {
            if (com.martinia.indigo.metadata.application.BookMetadataTitles.matches(title,
                    edition.path("title").asText(null), edition.path("subtitle").asText(null))) return edition;
        }
        return null;
    }

	private String encode(final String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private String normalize(final String value) {
		return StringUtils.stripAccents(StringUtils.defaultString(value))
				.replaceAll("[^a-zA-Z0-9]", " ")
				.replaceAll("\\s+", " ")
				.toLowerCase(java.util.Locale.ROOT)
				.trim();
	}
}
