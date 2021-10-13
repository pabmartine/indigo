package com.martinia.indigo.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.calibre.Author;
import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.model.indigo.MyAuthor;
import com.martinia.indigo.model.indigo.MyBook;
import com.martinia.indigo.repository.calibre.AuthorRepository;
import com.martinia.indigo.repository.calibre.BookRepository;
import com.martinia.indigo.repository.indigo.ConfigurationRepository;
import com.martinia.indigo.repository.indigo.MyAuthorRepository;
import com.martinia.indigo.repository.indigo.MyBookRepository;
import com.martinia.indigo.singletons.MetadataSingleton;
import com.martinia.indigo.utils.CoverComponent;
import com.martinia.indigo.utils.GoodReadsComponent;
import com.martinia.indigo.utils.GoogleBooksComponent;
import com.martinia.indigo.utils.WikipediaComponent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AsyncService {

	@Autowired
	private WikipediaComponent wikipediaComponent;

	@Autowired
	private GoodReadsComponent goodReadsComponent;

	@Autowired
	private GoogleBooksComponent googleBooksComponent;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private MyBookRepository myBookRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private MyAuthorRepository myAuthorRepository;

	@Autowired
	private MetadataSingleton metadataSingleton;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private CoverComponent coverComponent;

	@Async
	public void getAllNoData(String lang) {

		if (metadataSingleton.isRunning()) {
			stop();
		}

		metadataSingleton.start("nodata");

		long pullTime = Long.parseLong(configurationRepository.findById("metadata.pull")
				.get()
				.getValue());

		long countBooks = bookRepository.count();
		metadataSingleton.setTotal(countBooks);

		// For each book
		for (int i = 0; i < countBooks; i++) {

			try {

				boolean serviceCalled = false;

				if (!metadataSingleton.isRunning())
					break;

				metadataSingleton.setCurrent(i);

				// Get currentBook info
				Book book = bookRepository.findAll(PageRequest.of(i, 1))
						.getContent()
						.get(0);

				// Get ratting
				Optional<MyBook> myBook = myBookRepository.findById(book.getId());

				if (!myBook.isPresent()) {
					MyBook _myBook = goodReadsComponent.findBook(book.getTitle(), book.getAuthorSort());

					if (_myBook == null) {
						_myBook = googleBooksComponent.findBook(book.getTitle(), book.getAuthorSort());
					}

					if (_myBook != null) {
						_myBook.setId(book.getId());
						myBookRepository.save(_myBook);
					}

					serviceCalled = true;

					// Get author
					if (!book.getAuthorSort()
							.contains(" & ")) {// Get info if there is only one author

						Author author = authorRepository.findBySort(book.getAuthorSort());

						Optional<MyAuthor> myAuthor = myAuthorRepository.findById(author.getId());
						if (!myAuthor.isPresent()) {

							MyAuthor _myAuthor = wikipediaComponent.findAuthor(author.getSort(), lang);

							if (_myAuthor == null) {
								_myAuthor = wikipediaComponent.findAuthor(author.getSort(), "en");
							}

							if (_myAuthor == null || _myAuthor.getImage() == null) {
								MyAuthor _myAuthor2 = goodReadsComponent.findAuthor(author.getSort());
								if (_myAuthor2 != null) {
									_myAuthor = _myAuthor2;
								}
							}

							if (_myAuthor != null) {
								_myAuthor.setSort(author.getSort());
								_myAuthor.setId(author.getId());
								myAuthorRepository.save(_myAuthor);

							}
							serviceCalled = true;
						}
					}

					// Get book thumbail cover if needed
					coverComponent.getCover(book.getPath(), false);
				}

				if (serviceCalled) {
					Thread.sleep(pullTime);
				}

				log.info("Processed " + i + " books");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		stop();
	}

	@Async
	public void getAllData(String lang) {

		if (metadataSingleton.isRunning()) {
			stop();
		}

		metadataSingleton.start("data");

		long pullTime = Long.parseLong(configurationRepository.findById("metadata.pull")
				.get()
				.getValue());

		long countBooks = bookRepository.count();
		metadataSingleton.setTotal(countBooks);

		// For each book
		for (int i = 0; i < countBooks; i++) {

			try {
				if (!metadataSingleton.isRunning())
					break;

				metadataSingleton.setCurrent(i);

				// Get currentBook info
				Book book = bookRepository.findAll(PageRequest.of(i, 1))
						.getContent()
						.get(0);

				// Get ratting
				MyBook _myBook = goodReadsComponent.findBook(book.getTitle(), book.getAuthorSort());

				if (_myBook == null) {
					_myBook = googleBooksComponent.findBook(book.getTitle(), book.getAuthorSort());
				}

				if (_myBook != null) {
					_myBook.setId(book.getId());
					myBookRepository.save(_myBook);
				}

				// Get author
				if (!book.getAuthorSort()
						.contains(" & ")) {// Get info if there is only one author

					Author author = authorRepository.findBySort(book.getAuthorSort());

					MyAuthor _myAuthor = wikipediaComponent.findAuthor(author.getSort(), lang);

					if (_myAuthor == null) {
						_myAuthor = wikipediaComponent.findAuthor(author.getSort(), "en");
					}

					if (_myAuthor == null || _myAuthor.getImage() == null) {
						MyAuthor _myAuthor2 = goodReadsComponent.findAuthor(author.getSort());
						if (_myAuthor2 != null) {
							_myAuthor = _myAuthor2;
						}
					}

					if (_myAuthor != null) {
						_myAuthor.setSort(author.getSort());
						_myAuthor.setId(author.getId());
						myAuthorRepository.save(_myAuthor);

					}
				}

				// Get book thumbail cover if needed
				coverComponent.getCover(book.getPath(), true);

				// Sleept for 5 seconds
				Thread.sleep(pullTime);

				log.info("Processed " + i + " books");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		stop();
	}

	public Map<String, Object> getAllAuthorsState() {
		Map<String, Object> data = new HashMap<>();
		data.put("type", metadataSingleton.getType());
		data.put("status", metadataSingleton.isRunning());
		data.put("current", metadataSingleton.getCurrent());
		data.put("total", metadataSingleton.getTotal());
		return data;
	}

	public void stop() {
		log.info("Stopping async authors process");
		metadataSingleton.stop();
	}

}
