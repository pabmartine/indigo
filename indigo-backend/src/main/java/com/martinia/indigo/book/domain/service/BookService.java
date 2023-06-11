package com.martinia.indigo.book.domain.service;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.domain.model.Search;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

public interface BookService {

	Long count(Search search);

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	List<Book> getSimilar(List<String> similar, List<String> languages);

	List<Book> getSerie(String serie, List<String> languages);

	List<Book> getRecommendationsByBook(List<String> recommendations, List<String> languages);

	List<Book> getRecommendationsByUser(String user, int page, int size, String sort, String order);

	Long countRecommendationsByUser(String user);

	Resource getEpub(String path);

	List<String> getBookLanguages();

	Optional<String> getImage(String path);

}
