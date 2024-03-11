package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.model.Search;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FindSimilarBooksMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class FindSimilarBooksMetadataUseCaseImpl implements FindSimilarBooksMetadataUseCase {

	@Resource
	protected BookRepository bookRepository;

	@Override
	public void findSimilarBooks(final String bookId, final String similarBooks) {

		try {
			List<String> ret = new ArrayList<>();

			Optional.ofNullable(similarBooks).ifPresent(similar -> {
				String[] similars = similar.split("#;#");
				for (String s : similars) {
					String[] data = s.split("@;@");
					String title = data[0];
					String author = data[1];

					Search search = new Search();
					while (title.contains("(") && title.contains(")")) {
						String del = title.substring(title.indexOf("("), title.indexOf(")") + 1);
						title = title.replace(del, "");
					}
					while (title.contains("[") && title.contains("]")) {
						String del = title.substring(title.indexOf("["), title.indexOf("]") + 1);
						title = title.replace(del, "");
					}

					search.setTitle(title.replace("+", "")
							.replace("¿", "")
							.replace("?", "")
							.replace("*", "")
							.replace("(", "")
							.replace(")", "")
							.replace("[", "")
							.replace("]", "").trim());

					List<BookMongoEntity> books = bookRepository.findAll(search, 0, Integer.MAX_VALUE, "_id", "asc");
					if (!CollectionUtils.isEmpty(books)) {
						for (BookMongoEntity book : books) {
							String _authors = String.join(" ", book.getAuthors());

							String filterSimilarAuthor = StringUtils.stripAccents(author)
									.replaceAll("[^a-zA-Z0-9]", " ")
									.replaceAll("\\s+", " ")
									.toLowerCase()
									.trim();

							String[] similarTerms = StringUtils.stripAccents(_authors)
									.replaceAll("[^a-zA-Z0-9]", " ")
									.replaceAll("\\s+", " ")
									.split(" ");

							boolean similarContains = true;
							for (String similarTerm : similarTerms) {
								similarTerm = StringUtils.stripAccents(similarTerm).toLowerCase().trim();
								if (!filterSimilarAuthor.contains(similarTerm)) {
									similarContains = false;
								}
							}

							if (similarContains) {
								ret.add(book.getId());
							}
						}
					}
				}
			});

			if (!CollectionUtils.isEmpty(ret)) {
				bookRepository.findById(bookId).ifPresent(book -> {
					book.setSimilar(ret);
					bookRepository.save(book);
					log.info("Found similar books for {}", book.getTitle());
				});

			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}

	}
}
