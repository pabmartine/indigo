package com.martinia.indigo.services.calibre;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.dto.Search;
import com.martinia.indigo.model.calibre.Book;

public interface BookService {

	Optional<Book> findById(Integer id);

	List<Book> getBookRecommendationsByBook(int id);

	long count(Search search);

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	List<Book> getSimilar(String similar);

	List<Book> getBookRecommendationsByUser(int user);

}
