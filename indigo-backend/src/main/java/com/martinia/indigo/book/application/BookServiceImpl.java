package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.BookService;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;
import com.martinia.indigo.domain.model.Search;
import com.martinia.indigo.domain.util.UtilComponent;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

	@Autowired
	BookRepository bookRepository;

	@Autowired
	ConfigurationRepository configurationRepository;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	UtilComponent utilComponent;

	@Override
	public Long count(Search search) {
		return bookRepository.count(search);
	}

	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		return bookRepository.findAll(search, page, size, sort, order);
	}


	@Override
	public List<Book> getRecommendationsByBook(List<String> recommendations, List<String> languages) {
		int num = configurationRepository.findByKey("books.recommendations").map(rec -> Integer.parseInt(rec.getValue())).orElse(0);
		return bookRepository.getRecommendationsByBook(recommendations, languages, num);
	}

	@Override
	public Long countRecommendationsByUser(String user) {
		return bookRepository.countRecommendationsByUser(user);
	}

	@Override
	public List<Book> getRecommendationsByUser(String user, int page, int size, String sort, String order) {
		return bookRepository.getRecommendationsByUser(user, page, size, sort, order);
	}

	@Override
	public Resource getEpub(String path) {
		return utilComponent.getEpub(path.replace("@_@", "&").replace("@-@", "[").replace("@¡@", "]").replace("@!@", "`"));
	}

	@Override
	public List<Book> getSimilar(List<String> similar, List<String> languages) {
		return bookRepository.getSimilar(similar, languages);
	}

	@Override
	public List<Book> getSerie(String serie, List<String> languages) {
		return bookRepository.getSerie(serie, languages);
	}

	@Override
	public List<String> getBookLanguages() {
		return bookRepository.getBookLanguages();
	}

	@Override
	public Optional<String> getImage(String path) {
		return Optional.ofNullable(utilComponent.getBase64Cover(path, false));
	}
}
