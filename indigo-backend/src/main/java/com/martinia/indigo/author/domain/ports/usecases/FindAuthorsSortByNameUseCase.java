package com.martinia.indigo.author.domain.ports.usecases;

import com.martinia.indigo.author.domain.model.Author;

import java.util.Optional;

public interface FindAuthorsSortByNameUseCase {

	Optional<Author> findBySort(String name);

}
