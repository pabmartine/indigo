package com.martinia.indigo.services.calibre.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.martinia.indigo.dto.Search;
import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.repository.calibre.BookRepository;
import com.martinia.indigo.services.calibre.BookService;
import com.martinia.indigo.services.indigo.ConfigurationService;
import com.martinia.indigo.services.indigo.MyBookService;

@Service
public class BookServiceImpl implements BookService {

	@Autowired
	BookRepository bookRepository;

	@Autowired
	MyBookService myBookService;

	@Autowired
	ConfigurationService configurationService;

	@Override
	public Optional<Book> findById(Integer id) {
		return bookRepository.findById(id);
	}

	@Override
	public List<Book> getBookRecommendationsByBook(int id) {

		int max = Integer.parseInt(configurationService.findById("books.recommendations")
				.get()
				.getValue());

		return bookRepository.getBookRecommendations(id, PageRequest.of(0, max, Sort.by("id")));
	}

	@Override
	public long count(Search search) {
		return bookRepository.count(search);
	}

	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		return bookRepository.findAll(search, page, size, sort, order);
	}

	@Override
	public List<Book> getSimilar(String similar) {
		List<Book> list = new ArrayList<Book>();

		String[] data = similar.split(";");
		for (String id : data) {
			int bookId = Integer.parseInt(id);
			Book book = this.findById(bookId)
					.get();
			list.add(book);
		}

		return list;
	}

	@Override
	public List<Book> getBookRecommendationsByUser(int user) {

		int max = Integer.parseInt(configurationService.findById("books.recommendations2")
				.get()
				.getValue());

		Map<Integer, Book> map = new HashMap<>();
		List<Integer> ids = new ArrayList<>();
		List<Book> books = myBookService.getSentBooks(user);
		for (Book book : books) {
			ids.add(book.getId());
			List<Book> _books = bookRepository.getBookRecommendations(book.getId(), PageRequest.of(0, max, Sort.by("id")));
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
				.limit(max)
				.collect(Collectors.toList());
	}

}
