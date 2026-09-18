package com.martinia.indigo.author.domain.ports.usecases;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.model.AuthorPageData;

import java.util.List;

public interface FindAllAuthorsUseCase {

	List<Author> findAll(List<String> languages, int page, int size, String sort, String order);

	AuthorPageData findSummaryPage(List<String> languages, int page, int size, String sort, String order);

}

