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

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
@Transactional
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

		subject = StringUtils.stripAccents(subject).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");

		String url = endpoint.replace("$lang", lang).replace("$subject", subject.replace(" ", "%20"));

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

					String[] terms = subject.split(" ");

					long hasTerms = Arrays.stream(terms)
							.filter(term -> filterTitle.contains(StringUtils.stripAccents(term).toLowerCase().trim())).count();

					if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
						ret = new String[] { extract.asText(), source != null ? source.asText() : null, ProviderEnum.WIKIPEDIA.name() };
					}

				}
			}
		}
		catch (Exception e) {
			log.error(url);
		}

		if (ret!=null && !lang.equals("es") && !StringUtils.isEmpty(ret[0])){
			final String description = ret[0];
			ret[0] = translateLibreTranslatePort.map(libreTranslate -> libreTranslate.translate(description, "es"))
					.orElse(null);

		}

		return ret;
	}

}
