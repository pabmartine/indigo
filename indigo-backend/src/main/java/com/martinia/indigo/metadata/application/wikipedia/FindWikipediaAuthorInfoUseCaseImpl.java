package com.martinia.indigo.metadata.application.wikipedia;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorInfoUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Optional;

@Slf4j
@Service
public class FindWikipediaAuthorInfoUseCaseImpl implements FindWikipediaAuthorInfoUseCase {

	@Resource
	private DataUtils dataUtils;

	@Resource
	private Optional<TranslateLibreTranslatePort> translateLibreTranslatePort;

	@Value("${metadata.wikipedia.author-info}")
	private String endpoint;

	@Override
	public String[] getAuthorInfo(String subject, String lang) {

		String[] ret = null;

		String normalizedSubject = StringUtils.stripAccents(subject).replaceAll("[^a-zA-Z0-9]", " ")
                .replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT).trim();

		String url = endpoint.replace("$lang", lang).replace("$subject", java.net.URLEncoder.encode(subject, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));

		try {
			String json = dataUtils.getData(url);

			if (StringUtils.isNoneEmpty(json)) {

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

				JsonNode jsonNodeRoot = objectMapper.readTree(json);
				JsonNode query = jsonNodeRoot.get("query");
				if (query != null) {

					JsonNode search = query.get("pages");
					JsonNode title = search.findPath("title");
					JsonNode extract = search.findPath("extract");
					JsonNode source = search.findPath("original").get("source");

					String strTitle = title.asText();
					String filterTitle = StringUtils.stripAccents(strTitle).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
							.toLowerCase().trim();


					if (filterTitle.equals(normalizedSubject) || filterTitle.startsWith(normalizedSubject + " ")) {
						ret = new String[] { extract.asText(), source != null ? source.asText() : null, ProviderEnum.WIKIPEDIA.name() };
					}

				}
			}
		}
		catch (Exception e) {
			throw new IllegalStateException("Could not obtain Wikipedia author details from " + url, e);
		}

		if (ret != null && !"es".equals(com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation.normalizeLanguage(lang))
				&& StringUtils.isNotBlank(ret[0])) {
			final String description = ret[0];
			try {
				ret[0] = translateLibreTranslatePort.map(libreTranslate -> libreTranslate.translate(description, "es")).orElse(null);
				if (StringUtils.isBlank(ret[0])) throw new IllegalStateException("Spanish author translation unavailable; retry later");
			}
			catch (RuntimeException exception) {
				com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
				throw new com.martinia.indigo.metadata.application.AuthorMetadataTranslationException(
						"Spanish author translation unavailable; retry later", ret[1], ProviderEnum.WIKIPEDIA.name(), exception);
			}
		}

		return ret;
	}

}
