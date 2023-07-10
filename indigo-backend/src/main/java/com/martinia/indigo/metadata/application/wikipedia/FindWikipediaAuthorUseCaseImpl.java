package com.martinia.indigo.metadata.application.wikipedia;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorInfoUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

@Slf4j
@Service
public class FindWikipediaAuthorUseCaseImpl implements FindWikipediaAuthorUseCase {

	private String PROVIDER = "Wikipedia";

	@Resource
	private FindWikipediaAuthorInfoUseCase findWikipediaAuthorInfoUseCase;
	@Resource
	private DataUtils dataUtils;

	@Override
	public String[] findAuthor(String subject, String lang, int cont) {

		String[] ret = null;

		subject = StringUtils.stripAccents(subject).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");

		String url = "https://" + lang + ".wikipedia.org/w/api.php?action=query&format=json&list=search&utf8=1&origin=*&srsearch="
				+ subject.replace(" ", "%20");
		try {

			String json = dataUtils.getData(url);

			if (StringUtils.isNoneEmpty(json)) {

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

				JsonNode jsonNodeRoot = objectMapper.readTree(json);
				JsonNode query = jsonNodeRoot.get("query");
				JsonNode search = query.get("search");

				String strTitle = null;
				String[] terms = subject.split(" ");
				if (search.isArray() && !search.isEmpty()) {
					for (final JsonNode objNode : search) {
						JsonNode title = objNode.get("title");
						strTitle = title.asText();

						String filterTitle = StringUtils.stripAccents(strTitle).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
								.toLowerCase().trim();

						long hasTerms = Arrays.stream(terms)
								.filter(term -> filterTitle.contains(StringUtils.stripAccents(term).toLowerCase().trim())).count();

						if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
							break;
						}
						else {
							strTitle = null;
						}

					}
				}
				else {
					if (query.get("searchinfo") != null && query.get("searchinfo").get("suggestion") != null && cont < 2) {
						String auth = query.get("searchinfo").get("suggestion").asText();
						if (!StringUtils.stripAccents(auth).equals(StringUtils.stripAccents(subject))) {
							log.warn("	Retry with {}", auth);
							return findAuthor(auth, lang, ++cont);
						}
					}
				}

				if (StringUtils.isNotEmpty(strTitle)) {
					ret = findWikipediaAuthorInfoUseCase.getAuthorInfo(strTitle, lang);
				}
			}
		}
		catch (Exception e) {
			log.error(url);
		}

		return ret;
	}

}
