package com.martinia.indigo.ports.in.rest;

import java.util.List;

import com.martinia.indigo.domain.model.Author;

public interface AuthorService {

	Long count();

	List<Author> findAll(int page, int size, String sort, String order);

	Author findBySort(String name);

}