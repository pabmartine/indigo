package com.martinia.indigo.metadata.application.goodreads;

import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsBookUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(name = "flags.goodreads", havingValue = "true")
public class FindGoodReadsBookUseCaseImpl implements FindGoodReadsBookUseCase {

	@Value("${metadata.goodreads.book}")
	private String endpointBook;

	@Value("${metadata.goodreads.book-authors}")
	private String endpointBookAuthors;

	@Resource
	private DataUtils dataUtils;

	@Override
	public String[] findBook(String key, String title, List<String> authors, boolean withAuthor) {

		String[] ret = null;

		try {

			String author = String.join(" ", authors);

			author = StringUtils.stripAccents(author).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");
			title = StringUtils.stripAccents(title.replaceAll("ñ", "-")).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");

			String url = endpointBook.replace("$title", title.replace(" ", "-")).replace("$key", key);
			if (withAuthor) {
				url += endpointBookAuthors.replace("$title", title.replace(" ", "-")).replace("$key", key)
						.replace("$authors", author.replace(" ", "%20"));
			}

			String xml = dataUtils.getData(url);

			if (StringUtils.isNoneEmpty(xml)) {
				Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

				if (doc.select("book").first() != null) {

					String finalTitle = title;
					String finalAuthor = author;
					ret = doc.select("book").stream().map(book -> {

						String name = book.select("title").text();
						String filterName = StringUtils.stripAccents(name).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
								.toLowerCase().trim();

						String[] terms = finalTitle.split(" ");

						long hasTerms = Arrays.stream(terms).filter(term -> {
							return (filterName.contains(StringUtils.stripAccents(term).toLowerCase().trim()));
						}).count();

						if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {

							String _author = book.select("authors").select("author").select("name").text();

							String filterAuthor = StringUtils.stripAccents(_author).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
									.toLowerCase().trim();

							terms = finalAuthor.split(" ");

							hasTerms = Arrays.stream(terms).filter(term -> {
								return (filterAuthor.contains(StringUtils.stripAccents(term).toLowerCase().trim()));
							}).count();

							if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {

								float rating = Float.parseFloat(book.select("average_rating").get(0).text());
								String similar = book.select("similar_books").select("book").stream().map(similarBook -> {
									String similar_title = similarBook.select("title").text();
									String similar_author = similarBook.select("authors").select("author").select("name").text();

									return similar_title + "@;@" + similar_author;
								}).collect(Collectors.joining("#;#"));

								if (StringUtils.isNoneEmpty(similar)) {
									return new String[] { String.valueOf(rating), similar, ProviderEnum.GOODREADS.name() };
								}

							}

						}
						return null;
					}).filter(Objects::nonNull).findFirst().orElse(null);

				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}

		if (ret == null && !withAuthor) {
			ret = findBook(key, title, authors, true);
		}

		return ret;

	}

}
