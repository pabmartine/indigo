package com.martinia.indigo.services.calibre.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.calibre.Author;
import com.martinia.indigo.repository.calibre.AuthorRepository;
import com.martinia.indigo.services.calibre.AuthorService;

@Service
public class AuthorServiceImpl implements AuthorService {

	@Autowired
	AuthorRepository authorRepository;

	@Override
	public Author findBySort(String author) {
		return authorRepository.findBySort(author);
	}

	@Override
	public Object[] getNumBooksByAuthorId(Integer id) {
		return authorRepository.getNumBooksByAuthorId(id);
	}

	@Override
	public long count() {
		return authorRepository.count();
	}

	@Override
	public List<Object[]> getNumBooksByAuthor(int page, int size, String sort, String order) {
		return authorRepository.getNumBooksByAuthor(page, size, sort, order);
	}

}
