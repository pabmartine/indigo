package com.martinia.indigo.domain.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;
import com.martinia.indigo.domain.util.CoverComponent;
import com.martinia.indigo.ports.in.rest.BookService;
import com.martinia.indigo.ports.out.mongo.BookRepository;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;
import com.martinia.indigo.ports.out.mongo.NotificationRepository;

@Service
public class BookServiceImpl implements BookService {

	@Autowired
	BookRepository bookRepository;

	@Autowired
	ConfigurationRepository configurationRepository;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	CoverComponent coverComponent;

	@Override
	public Long count(Search search) {
		return bookRepository.count(search);
	}

	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		return bookRepository.findAll(search, page, size, sort, order);
	}

	@Override
	public List<Book> findBooksBySerie(String serie) {
		return bookRepository.findBooksBySerie(serie);
	}

	@Override
	public Book findById(String id) {
		return bookRepository.findById(id);
	}

	@Override
	public Book findByPath(String path) {
		return bookRepository.findByPath(path);

	}

	@Override
	public List<Book> getBookRecommendationsByBook(String id) {
		int num = Integer.parseInt(configurationRepository.findByKey("books.recommendations")
				.getValue());
		return bookRepository.getBookRecommendationsByBook(id, num);
	}

	@Override
	public List<Book> getBookRecommendationsByUser(String user) {
		int num = Integer.parseInt(configurationRepository.findByKey("books.recommendations2")
				.getValue());
		return bookRepository.getBookRecommendationsByUser(user, num);
	}

	@Override
	public String getCover(String path, boolean force) {
		return coverComponent.getCover(path, force);
	}

	@Override
	public Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order) {
		return bookRepository.getNumBooksBySerie(page, size, sort, order);
	}

	@Override
	public Long getNumSeries() {
		return bookRepository.getNumSeries();
	}

	@Override
	public List<Book> getSimilar(List<String> similar) {
		return bookRepository.getSimilar(similar);
	}

}
