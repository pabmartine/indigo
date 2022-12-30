package com.martinia.indigo.ports.in.rest;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.domain.model.Author;

public interface AuthorService {

	Long count(List<String> languages);

	List<Author> findAll(List<String> languages, int page, int size, String sort, String order);

	Optional<Author> findBySort(String name);

}
