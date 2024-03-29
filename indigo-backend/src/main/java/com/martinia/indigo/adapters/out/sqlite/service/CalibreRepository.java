package com.martinia.indigo.adapters.out.sqlite.service;

import java.util.List;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.common.model.Search;

public interface CalibreRepository {

	Long count(Search search);

	Book findBookById(String bookId);

	Book findBookByPath(String path);

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	List<Author> findAuthorsByBook(String book);


}
