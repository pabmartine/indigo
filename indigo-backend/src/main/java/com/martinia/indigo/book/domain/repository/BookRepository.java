package com.martinia.indigo.book.domain.repository;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.domain.model.Search;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

	Long count(Search search);

	void dropCollection();

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	Optional<Book> findById(String id);

	Optional<Book> findByPath(String path);

	List<Book> getRecommendationsByBook(List<String> recommendations, List<String> languages, int num);

	List<Book> getRecommendationsByBook(Book book);

	List<Book> getRecommendationsByUser(String user, int page, int size, String sort, String order);

	List<Book> getSimilar(List<String> similar, List<String> languages);

	List<Book> getSerie(String serie, List<String> languages);

	void save(Book book);

	Long countRecommendationsByUser(String user);

	List<String> getBookLanguages();

}
