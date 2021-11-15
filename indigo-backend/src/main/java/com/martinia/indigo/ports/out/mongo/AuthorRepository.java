package com.martinia.indigo.ports.out.mongo;

import java.util.List;

import com.martinia.indigo.domain.model.Author;

public interface AuthorRepository {

	Long count(List<String> languages);

	List<Author> findAll(List<String> languages, int page, int size, String sort, String order);

	Author findById(String id);

	Author findBySort(String sort);

	void save(Author author);

	void dropCollection();

	void update(Author author);

}
