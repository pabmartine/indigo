package com.martinia.indigo.metadata.application.wikipedia;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorInfoPort;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Slf4j
@Service
public class FindWikipediaAuthorUseCaseImpl implements FindWikipediaAuthorUseCase {

	@Value("${metadata.wikipedia.author}")
	private String endpoint;

	@Resource
	private FindWikipediaAuthorInfoPort findWikipediaAuthorInfoPort;
	@Resource
	private DataUtils dataUtils;

	@Override
	public String[] findAuthor(String subject, String lang, int cont) {
        return findAuthor(subject, lang, cont, true);
    }

    @Override
    public String[] findAuthor(String subject, String lang, int cont, boolean descriptionNeeded) {

		String[] ret = null;
        if (StringUtils.isBlank(subject)) return null;

		subject = StringUtils.stripAccents(subject).replaceAll("[^\\p{L}\\p{N}]", " ").replaceAll("\\s+", " ").trim();
        if (subject.isBlank()) return null;

		String url = endpoint.replace("$lang", lang).replace("$subject", java.net.URLEncoder.encode(subject, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));

		try {

			String json = dataUtils.getData(url);

			if (StringUtils.isNoneEmpty(json)) {

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

				JsonNode jsonNodeRoot = objectMapper.readTree(json);
				JsonNode query = jsonNodeRoot.path("query");
				JsonNode search = query.get("search");

				String strTitle = null;
                if (search == null || !search.isArray()) throw new IllegalStateException("Invalid Wikipedia search response");
                if (!search.isEmpty()) {
                    java.util.Set<String> exact = new java.util.LinkedHashSet<>();
                    java.util.Set<String> qualified = new java.util.LinkedHashSet<>();
                    for (JsonNode candidate : search) {
                        String title = candidate.path("title").asText("");
                        if (WikipediaAuthorTitles.normalize(subject).equals(WikipediaAuthorTitles.normalize(title))) exact.add(title);
                        else if (WikipediaAuthorTitles.matches(subject, title)) qualified.add(title);
                    }
                    if (exact.size() == 1) strTitle = exact.iterator().next();
                    else if (exact.isEmpty() && qualified.size() == 1) strTitle = qualified.iterator().next();
                }

				if (StringUtils.isNotEmpty(strTitle)) {
					ret = descriptionNeeded ? findWikipediaAuthorInfoPort.getAuthorInfo(strTitle, lang)
                            : findWikipediaAuthorInfoPort.getAuthorInfo(strTitle, lang, false);
				}
			}
		}
		catch (com.martinia.indigo.metadata.application.AuthorMetadataTranslationException exception) {
			throw exception;
		}
		catch (Exception e) {
			throw new IllegalStateException("Could not obtain Wikipedia metadata from " + url, e);
		}

		return ret;
	}

}
