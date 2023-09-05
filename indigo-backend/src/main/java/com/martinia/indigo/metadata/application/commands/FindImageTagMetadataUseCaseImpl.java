package com.martinia.indigo.metadata.application.commands;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindImageTagMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
public class FindImageTagMetadataUseCaseImpl implements FindImageTagMetadataUseCase {

	@Value("${metadata.unsplash.url}")
	private String endpoint;

	@Override
	public String find(final String term) {

		List<String> images = findImages(term);
		Collections.shuffle(images);

		return CollectionUtils.isEmpty(images) ? null : images.stream().findFirst().get();

	}

	private List<String> findImages(String term) {

		List<String> images = new ArrayList<>();

		try {

			WebClient webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			String tokenized_term = normalize(term);

			String url = endpoint.replace("$term", tokenized_term);
			HtmlPage page = webClient.getPage(url);

			page.getByXPath("//div[@class='MorZF']").stream().forEach(item -> {
				HtmlDivision htmlDivision = (HtmlDivision) item;
				String imageUrl = ((HtmlImage) htmlDivision.getFirstChild()).getAttribute("src");
				images.add(imageUrl);
			});

		}
		catch (Exception e) {
			log.error(e.getMessage());
		}

		return images;
	}

	private static String normalize(String title) {
		if (title.contains("(")) {
			title = title.substring(0, title.indexOf("(")) + title.substring(title.indexOf(")") + 1, title.length());
		}
		return Normalizer.normalize(title, Normalizer.Form.NFD)
				.toLowerCase()
				.replaceAll("[^\\p{ASCII}]", "")
				.replaceAll(" ", "-")
				.replaceAll(",", "")
				.replaceAll("\\.", "-")
				.replaceAll(":", "-")
				.replaceAll("\\+\\+", "-");
	}
}
