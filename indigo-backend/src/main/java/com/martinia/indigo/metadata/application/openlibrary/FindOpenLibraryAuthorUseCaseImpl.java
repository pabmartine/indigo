package com.martinia.indigo.metadata.application.openlibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.usecases.openlibrary.FindOpenLibraryAuthorUseCase;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@ConditionalOnProperty(name = "flags.openlibrary", havingValue = "true")
public class FindOpenLibraryAuthorUseCaseImpl implements FindOpenLibraryAuthorUseCase {

	@Value("${metadata.openlibrary.authors}")
	private String authorsEndpoint;

	@Value("${metadata.openlibrary.author-info}")
	private String authorInfoEndpoint;

	@Value("${metadata.openlibrary.author-image}")
	private String authorImageEndpoint;

	@Resource
	private DataUtils dataUtils;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation spanishTranslation;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String[] findAuthor(final String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}

		try {
			final String json = dataUtils.getData(authorsEndpoint.replace("$subject", encode(name)));
			if (StringUtils.isBlank(json)) {
				return null;
			}

			final String expectedName = normalize(name);
			for (JsonNode candidate : objectMapper.readTree(json).path("docs")) {
				if (normalize(candidate.path("name").asText()).equals(expectedName)) {
					return findAuthorInfo(candidate.path("key").asText());
				}
			}
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not obtain Open Library author metadata for " + name, exception);
		}
		return null;
	}

	private String[] findAuthorInfo(final String id) throws Exception {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		final String json = dataUtils.getData(authorInfoEndpoint.replace("$id", id));
		if (StringUtils.isBlank(json)) {
			return null;
		}

		final JsonNode author = objectMapper.readTree(json);
		final JsonNode bioNode = author.path("bio");
		final String description = bioNode.isTextual() ? bioNode.asText() : bioNode.path("value").asText(null);
		final String image = authorImageEndpoint.replace("$id", id);
		if (StringUtils.isBlank(description)) {
			return null;
		}
		if (spanishTranslation == null) throw new IllegalStateException("Spanish translation is not configured");
		String translated = spanishTranslation.translate(description);
		if (StringUtils.isBlank(translated)) throw new IllegalStateException("Spanish author translation unavailable; retry later");
		return new String[] { translated, image, ProviderEnum.OPEN_LIBRARY.name() };
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
