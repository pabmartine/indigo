package com.martinia.indigo.services.calibre.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.martinia.indigo.dto.Search;
import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.repository.calibre.BookRepository;
import com.martinia.indigo.services.calibre.BookService;

@Service
public class BookServiceImpl implements BookService {

	@Autowired
	BookRepository bookRepository;

	@Override
	public Optional<Book> findById(Integer id) {
		return bookRepository.findById(id);
	}

	@Override
	public List<Book> getBookRecommendations(int id, PageRequest of) {
		return bookRepository.getBookRecommendations(id, of);
	}

	@Override
	public long count(Search search) {
		return bookRepository.count(search);
	}

	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		return bookRepository.findAll(search, page, size, sort, order);
	}

}
