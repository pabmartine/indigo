package com.martinia.indigo.adapters.out.mongo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.BookMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.BookMongoRepository;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;
import com.martinia.indigo.ports.out.mongo.BookRepository;
import com.martinia.indigo.ports.out.mongo.NotificationRepository;

@Component
public class BookRepositoryImpl implements BookRepository {

	@Autowired
	BookMongoRepository bookMongoRepository;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	BookMongoMapper bookMongoMapper;

	@Override
	public Long count(Search search) {
		return bookMongoRepository.count(search);
	}


	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.findAll(search, page, size, sort, order));
	}

	@Override
	public List<Book> findBooksBySerie(String serie) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.findBooksBySerie(serie));
	}

	@Override
	public Book findById(String id) {
		return bookMongoMapper.entity2Domain(bookMongoRepository.findById(id)
				.orElse(null));
	}

	public Book findByTitle(String title) {
		return bookMongoMapper.entity2Domain(bookMongoRepository.findByTitle(title));

	}

	@Override
	public List<Book> getBookRecommendationsByBook(String id, int num) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getBookRecommendations(id, 0, num, "id", "ASC"));
	}

	@Override
	public List<Book> getBookRecommendationsByUser(String user, int num) {

		Map<String, Book> map = new HashMap<>();
		List<String> ids = new ArrayList<>();
		List<Book> books = notificationRepository.getSentBooks(user);
		for (Book book : books) {
			ids.add(book.getId());
			List<Book> _books = this.getBookRecommendationsByBook(book.getId(), num);
			for (Book _book : _books) {
				if (!map.containsKey(_book.getId()) && !ids.contains(_book.getId()))
					map.put(_book.getId(), _book);
			}
		}
		return map.values()
				.stream()
				.collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
					Collections.shuffle(collected);
					return collected.stream();
				}))
				.limit(num)
				.collect(Collectors.toList());
	}

	@Override
	public Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order) {
		return bookMongoRepository.getNumBooksBySerie(page, size, sort, order);
	}

	@Override
	public Long getNumSeries() {
		return bookMongoRepository.getNumSeries();
	}

	@Override
	public List<Book> getSimilar(List<String> similar) {
		List<Book> ret = new ArrayList<>(similar.size());
		for (String s : similar) {
			Book book = this.findByTitle(s);
			if (book != null)
				ret.add(book);
		}
		return ret;
	}

	@Override
	public void save(Book book) {
		bookMongoRepository.save(bookMongoMapper.domain2Entity(book));
	}


	@Override
	public void dropCollection() {
		bookMongoRepository.dropCollection(BookMongoEntity.class);
	}


	public Book findByPath(String path) {
		return bookMongoMapper.entity2Domain(bookMongoRepository.findByPath(path));

	}

}
