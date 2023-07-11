package com.martinia.indigo.metadata.application.common;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.infrastructure.api.mappers.ReviewDtoMapper;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.model.NumBooks;
import com.martinia.indigo.common.model.Review;
import com.martinia.indigo.common.model.Search;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.common.util.UtilComponent;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.metadata.domain.ports.adapters.amazon.FindAmazonReviewsPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsAuthorPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsBookPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsReviewsPort;
import com.martinia.indigo.metadata.domain.ports.adapters.google.FindGoogleBooksBookPort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorInfoPort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import com.martinia.indigo.adapters.out.sqlite.service.CalibreRepository;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BaseMetadataUseCaseImpl {

	@Resource
	protected BookRepository bookRepository;

	@Resource
	private TagRepository tagRepository;

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	protected ConfigurationRepository configurationRepository;

	@Resource
	private FindWikipediaAuthorPort findWikipediaAuthorPort;

	@Resource
	private FindWikipediaAuthorInfoPort findWikipediaAuthorInfoPort;

	@Resource
	private FindGoodReadsReviewsPort findGoodReadsReviewsPort;

	@Resource
	private FindGoodReadsAuthorPort findGoodReadsAuthorPort;

	@Resource
	private FindGoodReadsBookPort findGoodReadsBookPort;

	@Resource
	private FindGoogleBooksBookPort googleBooksComponent;

	@Resource
	private Optional<FindAmazonReviewsPort> findAmazonReviewsPort;

	@Resource
	private CalibreRepository calibreRepository;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	private UtilComponent utilComponent;

	@Resource
	private ReviewDtoMapper reviewDtoMapper;

	protected String goodreads;

	protected long pullTime;

	private final int BATCH_SIZE = 500;

	private long lastExecution;

	private void fillAuthors(String id, Book book) {

		boolean updateBook = false;

		// Authors
		if (!CollectionUtils.isEmpty(book.getAuthors())) {

			List<Author> authors = calibreRepository.findAuthorsByBook(id);

			if (!CollectionUtils.isEmpty(authors)) {

				for (Author author : authors) {

					if (author.getName().equals("VV., AA.") || author.getSort().equals("VV., AA.")) {
						author.setName("AA. VV.");
						author.setSort("AA. VV.");
					}

					if (!book.getAuthors().contains(author.getSort())) {

						String[] tokens = author.getSort().replace(",", "").split(" ");
						boolean _contains = false;
						for (String a : book.getAuthors()) {
							boolean contains = true;
							for (String t : tokens) {
								if (!a.contains(t)) {
									contains = false;
									break;
								}
							}
							if (contains) {
								_contains = true;

								if (!a.equals(author.getSort())) {
									author.setSort(a);
									updateBook = true;
								}
							}
						}

						if (!_contains) {
							book.getAuthors().add(author.getSort());
							updateBook = true;
						}

					}

					Author domainAuthor = new Author();
					domainAuthor.setName(author.getName());
					domainAuthor.setSort(author.getSort());
					domainAuthor.setNumBooks(new NumBooks());
					for (String lang : book.getLanguages()) {
						domainAuthor.getNumBooks().getLanguages().put(lang, 1);
					}

					authorRepository.save(domainAuthor);
				}

				if (updateBook == true) {
					String bookId = bookRepository.findByPath(book.getPath()).get().getId();
					book.setId(bookId);
					bookRepository.save(book);
				}
			}

		}

	}

	protected void fillMetadataAuthors(String lang, boolean override) {

		metadataSingleton.setMessage("obtaining_metadata_authors");

		List<String> languages = bookRepository.getBookLanguages();
		Long numAuthors = authorRepository.count(languages);

		metadataSingleton.setTotal(metadataSingleton.getTotal() + numAuthors);

		int page = 0;
		int size = BATCH_SIZE;
		while (page * size < numAuthors) {

			if (!metadataSingleton.isRunning()) {
				break;
			}

			List<Author> authors = authorRepository.findAll(languages, page, size, "id", "asc");

			if (!CollectionUtils.isEmpty(authors)) {
				for (Author author : authors) {

					if (!metadataSingleton.isRunning()) {
						break;
					}

					metadataSingleton.setCurrent(metadataSingleton.getCurrent() + 1);

					author = findAuthorMetadata(lang, override, author);

					authorRepository.update(author);

					log.debug("Obtained {}/{} authors metadata", metadataSingleton.getCurrent(), numAuthors);

				}
			}

			log.info("Obtained {}/{} authors metadata", metadataSingleton.getCurrent(), numAuthors);
			page++;

		}

		log.info("Obtained {}/{} authors metadata", metadataSingleton.getCurrent(), numAuthors);

	}

	protected void fillMetadataBooks(boolean override) {

		metadataSingleton.setMessage("obtaining_metadata_books");

		Long numBooks = bookRepository.count(null);

		metadataSingleton.setTotal(metadataSingleton.getTotal() + numBooks);

		int page = 0;
		int size = BATCH_SIZE;
		while (page * size < numBooks) {

			if (!metadataSingleton.isRunning()) {
				break;
			}

			List<Book> books = bookRepository.findAll(null, page, size, "id", "asc");

			if (!CollectionUtils.isEmpty(books)) {
				for (Book book : books) {

					if (!metadataSingleton.isRunning()) {
						break;
					}

					metadataSingleton.setCurrent(metadataSingleton.getCurrent() + 1);

					book = findBookRecommendations(book);
					book = findBookMetadata(override, book);

					bookRepository.save(book);

					log.debug("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);
				}
			}

			log.info("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);
			page++;

		}

		log.info("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);

	}

	protected void fillMetadataReviews(String lang, boolean override) {

		metadataSingleton.setMessage("obtaining_metadata_reviews");

		Long numBooks = bookRepository.count(null);

		metadataSingleton.setTotal(metadataSingleton.getTotal() + numBooks);

		int page = 0;
		int size = BATCH_SIZE;
		while (page * size < numBooks) {

			if (!metadataSingleton.isRunning()) {
				break;
			}

			List<Book> books = bookRepository.findAll(null, page, size, "id", "asc");

			if (!CollectionUtils.isEmpty(books)) {
				for (Book book : books) {

					if (!metadataSingleton.isRunning()) {
						break;
					}

					metadataSingleton.setCurrent(metadataSingleton.getCurrent() + 1);

					book.setReviews(findReviewMetadata(override, lang, book));

					bookRepository.save(book);

					log.debug("Obtained {}/{} books reviews", metadataSingleton.getCurrent(), numBooks);
				}
			}

			log.info("Obtained {}/{} books reviews", metadataSingleton.getCurrent(), numBooks);
			page++;

		}

		log.info("Obtained {}/{} books reviews", metadataSingleton.getCurrent(), numBooks);

	}

	private boolean refreshAuthorMetadata(final Author author) {
		return (author == null || StringUtils.isEmpty(author.getDescription()) || StringUtils.isEmpty(author.getImage())
				|| StringUtils.isEmpty(author.getProvider())) && (author.getLastMetadataSync() == null || author.getLastMetadataSync()
				.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(7)
				.isBefore(Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
	}

	protected Author findAuthorMetadata(String lang, boolean override, Author author) {
		if (override || refreshAuthorMetadata(author)) {
			try {

				author.setDescription(null);
				author.setImage(null);
				author.setProvider(null);

				String[] wikipedia = findWikipediaAuthorPort.findAuthor(author.getName(), lang, 0);

				if (wikipedia == null && !lang.equals("en")) {
					wikipedia = findWikipediaAuthorPort.findAuthor(author.getName(), "en", 0);
				}

				if (wikipedia == null || wikipedia[1] == null) {
					String[] goodReads = findGoodReadsAuthorPort.findAuthor(goodreads, author.getName());
					if (goodReads != null) {
						author.setDescription(goodReads[0]);
						author.setImage(goodReads[1]);
						author.setProvider(goodReads[2]);

						Thread.sleep(pullTime);
					}

				}

				if (wikipedia != null) {
					if (StringUtils.isEmpty(author.getDescription()) && !StringUtils.isEmpty(wikipedia[0])) {
						author.setDescription(wikipedia[0]);
					}
					if (StringUtils.isEmpty(author.getImage()) && !StringUtils.isEmpty(wikipedia[1])) {
						author.setImage(wikipedia[1]);
					}
					if (StringUtils.isEmpty(author.getProvider()) && !StringUtils.isEmpty(wikipedia[2])) {
						author.setProvider(wikipedia[2]);
					}
				}

				if (!StringUtils.isEmpty(author.getImage())) {
					author.setImage(utilComponent.getBase64Url(author.getImage()));
				}

				if (StringUtils.isEmpty(author.getImage())) {
					Search search = new Search();
					search.setAuthor(author.getSort());
					List<Book> books = bookRepository.findAll(search, 0, Integer.MAX_VALUE, "pubDate", "desc");
					for (Book book : books) {
						String image = utilComponent.getImageFromEpub(book.getPath(), "autor", "author");
						author.setImage(image);
						if (author.getImage() != null) {
							break;
						}
					}

				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				author.setLastMetadataSync(Calendar.getInstance().getTime());
			}
		}
		return author;
	}

	private boolean refreshBookMetadata(final Book book) {
		return (book.getRating() == 0 || book.getProvider() == null || CollectionUtils.isEmpty(book.getSimilar())) && (
				book.getLastMetadataSync() == null || book.getLastMetadataSync().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDateTime().plusDays(7)
						.isBefore(Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
	}

	protected Book findBookMetadata(boolean override, Book book) {
		if (override || refreshBookMetadata(book)) {

			try {

				long seconds = ((System.currentTimeMillis() - lastExecution) / 1000);

				if (seconds < 1) {
					Thread.sleep(pullTime);
				}

				String[] bookData = findGoodReadsBookPort.findBook(goodreads, book.getTitle(), book.getAuthors(), false);

				lastExecution = System.currentTimeMillis();

				// Ratings && similar books
				if (bookData != null) {
					book.setRating(Float.valueOf(bookData[0]));
					book.setProvider(bookData[2]);

					if (StringUtils.isNotEmpty(bookData[1])) {
						List<String> similars = findSimilarBooks(bookData[1]);
						if (!CollectionUtils.isEmpty(similars)) {
							book.setSimilar(similars);
						}
					}

				}
				else {
					bookData = googleBooksComponent.findBook(book.getTitle(), book.getAuthors());

					if (bookData != null) {
						book.setRating(Float.valueOf(bookData[0]));
						book.setProvider(bookData[1]);
					}
				}

				//Find reviews
				List<Review> reviews = findReviewMetadata(override, "es", book);
				if (!CollectionUtils.isEmpty(reviews)) {
					book.setReviews(reviews);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				book.setLastMetadataSync(Calendar.getInstance().getTime());
			}
		}
		return book;
	}

	private boolean refreshReviewMetadata(final List<Review> reviews) {
		return CollectionUtils.isEmpty(reviews) || reviews.stream().anyMatch(review -> {
			LocalDateTime lastMetadataSync = Optional.ofNullable(review.getLastMetadataSync()).map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()).orElse(LocalDateTime.now());
			LocalDateTime currentDate = LocalDateTime.now();
			LocalDateTime expirationDate = lastMetadataSync.plusDays(7);
			return expirationDate.isBefore(currentDate);
		});
	}

	private List<Review> findReviewMetadata(boolean override, String lang, Book book) {
		if (override || refreshReviewMetadata(book.getReviews())) {
			List<ReviewDto> reviews = findGoodReadsReviewsPort.getReviews(lang, book.getTitle(), book.getAuthors());
			if (CollectionUtils.isEmpty(reviews)) {
				reviews = findAmazonReviewsPort.map(amazon -> amazon.getReviews(book.getTitle(), book.getAuthors())).orElse(null);
			}
			return reviewDtoMapper.dtos2domains(reviews);
		}
		return book.getReviews();
	}

	private Book findBookRecommendations(Book book) {

		List<Book> recommendations = bookRepository.getRecommendationsByBook(book);
		if (!CollectionUtils.isEmpty(recommendations)) {
			book.setRecommendations(new ArrayList<>());
			recommendations.forEach(b -> book.getRecommendations().add(b.getId()));
		}

		return book;
	}

	private List<String> findSimilarBooks(String similar) {

		List<String> ret = new ArrayList<>();

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

			search.setTitle(title.replace("+", "").replace("¿", "").replace("?", "").replace("*", "").replace("(", "").replace(")", "")
					.replace("[", "").replace("]", ""));

			List<Book> books = bookRepository.findAll(search, 0, Integer.MAX_VALUE, "_id", "asc");
			if (!CollectionUtils.isEmpty(books)) {
				for (Book book : books) {
					String _authors = String.join(" ", book.getAuthors());

					String filterSimilarAuthor = StringUtils.stripAccents(author).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
							.toLowerCase().trim();

					String[] similarTerms = StringUtils.stripAccents(_authors).replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ")
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

		return ret;
	}

	protected void initialLoad(String lang) {

		metadataSingleton.setMessage("indexing_books");

		tagRepository.deleteAll();
		authorRepository.deleteAll();
		bookRepository.deleteAll();

		Long numBooks = calibreRepository.count(null);
		metadataSingleton.setTotal(metadataSingleton.getTotal() + numBooks);

		int page = 0;
		int size = BATCH_SIZE;

		while (page * size < numBooks) {

			if (!metadataSingleton.isRunning()) {
				break;
			}

			List<Book> books = calibreRepository.findAll(null, page, size, "id", "asc");

			for (Book book : books) {
				if (!metadataSingleton.isRunning()) {
					break;
				}

				metadataSingleton.setCurrent(metadataSingleton.getCurrent() + 1);

				try {
					String id = book.getId();
					String image = utilComponent.getBase64Cover(book.getPath(), true);
					book.setImage(image);
					book.setId(null);
					tagRepository.save(book.getTags(), book.getLanguages());
					bookRepository.save(book);

					fillAuthors(id, book);

				}
				catch (Exception e) {
					e.printStackTrace();
				}

				log.debug("Indexed {}/{} books", metadataSingleton.getCurrent(), numBooks);

			}

			log.info("Indexed {}/{} books", metadataSingleton.getCurrent(), numBooks);

			page++;

		}

		fillMetadataBooks(true);
		fillMetadataAuthors(lang, true);

		stop();
	}

	protected void noFilledMetadata(String lang) {

		metadataSingleton.setMessage("obtaining_metadata");

		fillMetadataBooks(false);
		fillMetadataAuthors(lang, false);

		stop();
	}

	protected void stop() {
		log.info("Stopping async process");
		metadataSingleton.stop();
	}

}
