package com.martinia.indigo.ports.out.calibre;

import java.util.List;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;

public interface CalibreRepository {

	Long count(Search search);

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	List<Author> findAuthorsByBook(String book);

}
