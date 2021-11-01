package com.martinia.indigo.domain.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.singletons.MetadataSingleton;
import com.martinia.indigo.domain.util.CoverComponent;
import com.martinia.indigo.ports.in.rest.MetadataService;
import com.martinia.indigo.ports.out.calibre.CalibreRepository;
import com.martinia.indigo.ports.out.metadata.GoodReadsService;
import com.martinia.indigo.ports.out.metadata.GoogleBooksService;
import com.martinia.indigo.ports.out.metadata.WikipediaService;
import com.martinia.indigo.ports.out.mongo.AuthorRepository;
import com.martinia.indigo.ports.out.mongo.BookRepository;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;
import com.martinia.indigo.ports.out.mongo.TagRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetadataServiceImpl implements MetadataService {

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private WikipediaService wikipediaComponent;

	@Autowired
	private GoodReadsService goodReadsComponent;

	@Autowired
	private GoogleBooksService googleBooksComponent;

	@Autowired
	private CalibreRepository calibreRepository;

	@Autowired
	private MetadataSingleton metadataSingleton;

	@Autowired
	private CoverComponent coverComponent;

	private String goodreads;

	private final int BATCH_SIZE = 500;

	@Override
	@Async
	public void initialLoad(String lang) {

		if (metadataSingleton.isRunning()) {
			stop();
		}

		metadataSingleton.start("full");
		metadataSingleton.setMessage("indexing_books");

		goodreads = configurationRepository.findByKey("goodreads.key")
				.getValue();

		tagRepository.dropCollection();
		authorRepository.dropCollection();
		bookRepository.dropCollection();

		Long numBooks = calibreRepository.count(null);
		metadataSingleton.setTotal(numBooks * 3);

		int cont = 0;
		int page = 0;
		int size = BATCH_SIZE;

		while (page * size < numBooks) {

			if (!metadataSingleton.isRunning())
				break;

			List<Book> books = calibreRepository.findAll(null, page, size, "id", "asc");

			for (Book book : books) {

				if (!metadataSingleton.isRunning())
					break;

				metadataSingleton.setCurrent(cont++);

				try {
					coverComponent.getCover(book.getPath(), false);

					// Authors
					if (!CollectionUtils.isEmpty(book.getAuthors())) {

						List<Author> authors = calibreRepository.findAuthorsByBook(book.getId());

						if (!CollectionUtils.isEmpty(authors)) {

							for (Author author : authors) {

								com.martinia.indigo.domain.model.Author domainAuthor = new com.martinia.indigo.domain.model.Author();
								domainAuthor.setName(author.getName());
								domainAuthor.setSort(author.getSort());
								domainAuthor.setNumBooks(1);

								authorRepository.save(domainAuthor);

							}

						}

					}

					tagRepository.save(book.getTags());
					bookRepository.save(book);
				} catch (Exception e) {
					e.printStackTrace();
				}

				log.debug("Indexed {}/{} books", cont, numBooks);

			}

			log.info("Indexed {}/{} books", cont, numBooks);

			page++;

		}

		fillRecommendations(cont);
		fillMetadata(lang, cont);
		stop();
	}

	private void fillRecommendations(int cont) {

		metadataSingleton.setMessage("filling_recommendations");

		Long numBooks = bookRepository.count(null);

		int page = 0;
		int size = BATCH_SIZE;
		while (page * size < numBooks) {

			if (!metadataSingleton.isRunning())
				break;

			List<Book> books = bookRepository.findAll(null, page, size, "id", "asc");

			for (Book book : books) {

				if (!metadataSingleton.isRunning())
					break;

				metadataSingleton.setCurrent(cont++);

				List<Book> recommendations = bookRepository.getRecommendationsByBook(book.getId());
				if (!CollectionUtils.isEmpty(recommendations)) {
					book.setRecommendations(new ArrayList<>());
					recommendations.forEach(b -> book.getRecommendations()
							.add(b.getId()));
					bookRepository.save(book);
				}

				log.debug("Generated {}/{} recommendations", cont - numBooks, numBooks);

			}

			log.info("Generated {}/{} recommendations", cont - numBooks, numBooks);
			page++;
		}

	}

	private void fillMetadata(String lang, int cont) {

		metadataSingleton.setMessage("obtaining_metadata");

		long pullTime = Long.parseLong(configurationRepository.findByKey("metadata.pull")
				.getValue());

		Long numBooks = bookRepository.count(null);

		int page = 0;
		int size = BATCH_SIZE;
		while (page * size < numBooks) {

			if (!metadataSingleton.isRunning())
				break;

			List<Book> books = bookRepository.findAll(null, page, size, "id", "asc");

			for (Book book : books) {

				if (!metadataSingleton.isRunning())
					break;

				metadataSingleton.setCurrent(cont++);

				try {
					String[] goodReads = goodReadsComponent.findBook(goodreads, books, book.getTitle(),
							book.getAuthors());

					boolean updateBook = false;
					// Ratings && similar books
					if (goodReads != null) {
						book.setRating(Float.valueOf(goodReads[0]));
						book.setProvider(goodReads[2]);

						if (StringUtils.isNotEmpty(goodReads[1]))
							book.setSimilar(Arrays.asList(goodReads[1].split("\\\\s*;\\\\s*")));

						updateBook = true;
					} else {
						String[] googleBooks = googleBooksComponent.findBook(book.getTitle(), book.getAuthors());

						if (googleBooks != null) {
							book.setRating(Float.valueOf(googleBooks[0]));
							book.setProvider(googleBooks[1]);

							updateBook = true;
						}
					}

					if (updateBook)
						bookRepository.save(book);

					// Authors
					if (!CollectionUtils.isEmpty(book.getAuthors())) {
						for (String authorSort : book.getAuthors()) {

							Author author = authorRepository.findBySort(authorSort);

							String[] wikipedia = wikipediaComponent.findAuthor(authorSort, lang);

							if (wikipedia == null && !lang.equals("en")) {
								wikipedia = wikipediaComponent.findAuthor(authorSort, "en");
							}

							if (wikipedia == null || wikipedia[1] == null) {
								String[] _goodReads = goodReadsComponent.findAuthor(goodreads, authorSort);
								if (_goodReads != null) {
									author.setDescription(_goodReads[0]);
									author.setImage(_goodReads[1]);
									author.setProvider(_goodReads[2]);
								}
							}

							if (wikipedia != null) {
								author.setDescription(wikipedia[0]);
								author.setImage(wikipedia[1]);
								author.setProvider(wikipedia[2]);

								authorRepository.save(author);
							}

						}
					}

					Thread.sleep(pullTime);

				} catch (Exception e) {
					e.printStackTrace();
				}

				log.debug("Obtained {}/{} metadata", cont - numBooks, numBooks);
			}

			log.info("Obtained {}/{} metadata", cont - numBooks, numBooks);
			page++;
		}

	}

	@Override
	@Async
	public void noFilledMetadata(String lang) {

		if (metadataSingleton.isRunning()) {
			stop();
		}

		metadataSingleton.start("partial");
		metadataSingleton.setMessage("obtaining_metadata");

		goodreads = configurationRepository.findByKey("goodreads.key")
				.getValue();

		long pullTime = Long.parseLong(configurationRepository.findByKey("metadata.pull")
				.getValue());

		Long numBooks = bookRepository.count(null);
		metadataSingleton.setTotal(numBooks);

		int cont = 0;
		int updatedBooks = 0;
		int updatedAuthors = 0;
		int toUpdateBooks = 0;
		int toUpdateAuthors = 0;
		int page = 0;
		int size = BATCH_SIZE;

		while (page * size < numBooks) {

			if (!metadataSingleton.isRunning())
				break;

			List<Book> books = bookRepository.findAll(null, page, size, "id", "asc");

			for (Book book : books) {

				if (!metadataSingleton.isRunning())
					break;

				metadataSingleton.setCurrent(cont++);

				try {

					boolean has2Sleep = false;

					if (book.getRating() == 0 || book.getProvider() == null
							|| CollectionUtils.isEmpty(book.getSimilar())) {

						toUpdateBooks++;

						String[] goodReads = goodReadsComponent.findBook(goodreads, books, book.getTitle(),
								book.getAuthors());

						boolean updateBook = false;

						// Ratings && similar books
						if (goodReads != null) {
							book.setRating(Float.valueOf(goodReads[0]));
							book.setProvider(goodReads[2]);

							if (StringUtils.isNotEmpty(goodReads[1]))
								book.setSimilar(Arrays.asList(goodReads[1].split("\\\\s*;\\\\s*")));

							updateBook = true;
						} else {
							String[] googleBooks = googleBooksComponent.findBook(book.getTitle(), book.getAuthors());

							if (googleBooks != null) {
								book.setRating(Float.valueOf(googleBooks[0]));
								book.setProvider(googleBooks[1]);

								updateBook = true;
							}
						}

						if (updateBook) {
							bookRepository.save(book);
							updatedBooks++;
						}

						has2Sleep = true;
					}

					// Authors
					if (!CollectionUtils.isEmpty(book.getAuthors())) {
						for (String authorSort : book.getAuthors()) {

							Author author = authorRepository.findBySort(authorSort);

							if (author == null || StringUtils.isEmpty(author.getDescription())
									|| StringUtils.isEmpty(author.getImage())
									|| StringUtils.isEmpty(author.getProvider())) {

								toUpdateAuthors++;

								String[] wikipedia = wikipediaComponent.findAuthor(authorSort, lang);

								if (wikipedia == null && !lang.equals("en")) {
									wikipedia = wikipediaComponent.findAuthor(authorSort, "en");
								}

								if (wikipedia == null || wikipedia[1] == null) {
									String[] _goodReads = goodReadsComponent.findAuthor(goodreads, authorSort);
									if (_goodReads != null) {
										author.setDescription(_goodReads[0]);
										author.setImage(_goodReads[1]);
										author.setProvider(_goodReads[2]);
									}
								}

								if (wikipedia != null) {
									author.setDescription(wikipedia[0]);
									author.setImage(wikipedia[1]);
									author.setProvider(wikipedia[2]);

									authorRepository.save(author);
									updatedAuthors++;
								}

								has2Sleep = true;
							}
						}
					}

					if (has2Sleep)
						Thread.sleep(pullTime);

				} catch (Exception e) {
					e.printStackTrace();
				}

				log.debug("Obtained {}/{} metadata", cont, numBooks);

			}

			log.info("Obtained {}/{} metadata", cont, numBooks);
			page++;
		}

		log.info("Updated {}/{} books", updatedBooks, toUpdateBooks);
		log.info("Updated {}/{} authors", updatedAuthors, toUpdateAuthors);

	}

	public Map<String, Object> getStatus() {
		Map<String, Object> data = new HashMap<>();
		data.put("type", metadataSingleton.getType());
		data.put("status", metadataSingleton.isRunning());
		data.put("current", metadataSingleton.getCurrent());
		data.put("total", metadataSingleton.getTotal());
		data.put("message", metadataSingleton.getMessage());
		return data;
	}

	public void stop() {
		log.info("Stopping async authors process");
		metadataSingleton.stop();
	}

}
