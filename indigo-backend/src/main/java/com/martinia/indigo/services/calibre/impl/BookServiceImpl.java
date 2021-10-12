package com.martinia.indigo.services.calibre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.martinia.indigo.dto.Search;
import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.repository.calibre.BookRepository;
import com.martinia.indigo.services.calibre.BookService;
import com.martinia.indigo.services.indigo.ConfigurationService;

@Service
public class BookServiceImpl implements BookService {

	@Autowired
	BookRepository bookRepository;

	@Autowired
	ConfigurationService configurationService;

	@Override
	public Optional<Book> findById(Integer id) {
		return bookRepository.findById(id);
	}

	@Override
	public List<Book> getBookRecommendations(int id) {

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

}
