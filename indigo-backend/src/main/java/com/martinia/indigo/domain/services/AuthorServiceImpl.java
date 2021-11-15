package com.martinia.indigo.domain.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.ports.in.rest.AuthorService;
import com.martinia.indigo.ports.out.mongo.AuthorRepository;

@Service
public class AuthorServiceImpl implements AuthorService {

	@Autowired
	AuthorRepository authorRepository;

	@Override
	public Long count(List<String> languages) {
		return authorRepository.count(languages);
	}

	@Override
	public List<Author> findAll(List<String> languages, int page, int size, String sort, String order) {
		return authorRepository.findAll(languages, page, size, sort, order);
	}

	@Override
	public Author findBySort(String name) {
		return authorRepository.findBySort(name);

	}

}
