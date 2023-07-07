package com.martinia.indigo.book.domain.service;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.common.model.Search;

import java.util.List;

public interface FindAllBooksUseCase {

	List<Book> findAll(Search search, int page, int size, String sort, String order);

}
