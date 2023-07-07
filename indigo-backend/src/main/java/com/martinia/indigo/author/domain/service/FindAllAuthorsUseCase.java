package com.martinia.indigo.author.domain.service;

import com.martinia.indigo.author.domain.model.Author;

import java.util.List;

public interface FindAllAuthorsUseCase {

	List<Author> findAll(List<String> languages, int page, int size, String sort, String order);

}
