package com.martinia.indigo.metadata.application.goodreads;

import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsAuthorUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.Normalizer;
import java.util.Arrays;

@Slf4j
@Service
public class FindGoodReadsAuthorUseCaseImpl implements FindGoodReadsAuthorUseCase {

	private String endpoint = "https://www.goodreads.com/";

	@Resource
	private DataUtils dataUtils;

	private static String normalize(String title) {
		if (title.contains("(")) {
			title = title.substring(0, title.indexOf("(")) + title.substring(title.indexOf(")") + 1, title.length());
		}
		return Normalizer.normalize(title, Normalizer.Form.NFD).toLowerCase().replaceAll("[^\\p{ASCII}]", "").replaceAll(" ", "+")
				.replaceAll(",", "").replaceAll("\\.", "+").replaceAll(":", "+").replaceAll("\\+\\+", "+");
	}

	@Override
	public String[] findAuthor(String key, String subject) {

		String[] ret = null;

		try {

			subject = StringUtils.stripAccents(subject).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");

			String url = endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key;
			String xml = dataUtils.getData(url);

			if (StringUtils.isNoneEmpty(xml)) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
				if (doc.select("author").first() != null) {
					String name = doc.select("author").select("name").get(0).text();
					String id = doc.select("author").select("id").get(0).text();

					if (name != null && id != null) {

						String filterName = StringUtils.stripAccents(name).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
								.toLowerCase().trim();

						String[] terms = subject.split(" ");

						long hasTerms = Arrays.stream(terms).filter(term -> {
							return (filterName.contains(StringUtils.stripAccents(term).toLowerCase().trim()));
						}).count();

						if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
							ret = getAuthorInfo(key, id);
						}
						else {
							log.debug("************************** " + subject + " is not contained in " + name);
						}

					}
				}
			}
		}
		catch (Exception e) {
			log.error(endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key);
		}

		return ret;

	}

	private String[] getAuthorInfo(String key, String id) {

		String[] ret = null;

		try {
			String url = endpoint + "author/show/" + id + "?format=xml&key=" + key;
			String xml = dataUtils.getData(url);

			if (xml != null) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
				if (doc.select("author").first() != null) {
					String name = doc.select("author").select("name").get(0).text();
					String description = doc.select("author").select("about").text();
					String image = doc.select("author").select("image_url").get(0).text();

					if (StringUtils.isNotEmpty(name)) {
						ret = new String[] { description, image, ProviderEnum.GOODREADS.name() };
					}
				}
			}

		}
		catch (Exception e) {
			log.error(endpoint + "author/show/" + id + "?format=xml&key=" + key);
		}

		return ret;

	}

}
