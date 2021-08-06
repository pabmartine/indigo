package com.martinia.indigo.repository.calibre;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.martinia.indigo.dto.Search;
import com.martinia.indigo.model.calibre.Book;

@Repository
public interface CustomBookRepository {
	
	List<Book> findAll(Search search, int page, int size, String sort, String order);

	long count(Search search);

}
