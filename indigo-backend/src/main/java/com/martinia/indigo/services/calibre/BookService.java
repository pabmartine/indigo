package com.martinia.indigo.services.calibre;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;

import com.martinia.indigo.dto.Search;
import com.martinia.indigo.model.calibre.Book;

public interface BookService {

	Optional<Book> findById(Integer id);

	List<Book> getBookRecommendations(int id, PageRequest of);

	long count(Search search);

	List<Book> findAll(Search search, int page, int size, String sort, String order);

}
