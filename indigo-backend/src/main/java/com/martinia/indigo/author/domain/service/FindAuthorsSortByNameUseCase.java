package com.martinia.indigo.author.domain.service;

import com.martinia.indigo.author.domain.model.Author;

import java.util.Optional;

public interface FindAuthorsSortByNameUseCase {

	Optional<Author> findBySort(String name);

}
