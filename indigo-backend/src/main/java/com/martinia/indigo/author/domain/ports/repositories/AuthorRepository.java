package com.martinia.indigo.author.domain.ports.repositories;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.author.domain.model.Author;

public interface AuthorRepository {

	Long count(List<String> languages);

	List<Author> findAll(List<String> languages, int page, int size, String sort, String order);

	Optional<Author> findById(String id);

	Optional<Author> findBySort(String sort);

	void save(Author author);

	void deleteAll();

	void update(Author author);

}
