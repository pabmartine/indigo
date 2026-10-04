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


		String url = endpoint.replace("$lang", lang).replace("$subject", java.net.URLEncoder.encode(subject, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));

		try {
			String json = dataUtils.getData(url);

			if (StringUtils.isNoneEmpty(json)) {

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

				JsonNode jsonNodeRoot = objectMapper.readTree(json);
                JsonNode query = jsonNodeRoot.path("query");
                JsonNode pages = query.path("pages");
                if (!pages.isContainerNode()) throw new IllegalStateException("Invalid Wikipedia page response");
                for (JsonNode page : pages) {
                    if (page.has("missing") || page.has("invalid") || page.path("pageprops").has("disambiguation")) continue;
                    String title = page.path("title").asText("");
                    boolean matches = WikipediaAuthorTitles.matches(subject, title);
                    for (JsonNode redirect : query.path("redirects")) {
                        if (WikipediaAuthorTitles.normalize(subject).equals(WikipediaAuthorTitles.normalize(redirect.path("from").asText()))
                                && title.equals(redirect.path("to").asText())) matches = true;
                    }
                    if (!matches) continue;
                    String description = page.path("extract").asText(null);
                    String image = page.path("original").path("source").asText(null);
                    if (StringUtils.isNotBlank(description) || StringUtils.isNotBlank(image))
                        ret = new String[]{description, image, ProviderEnum.WIKIPEDIA.name()};
                    break;
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
