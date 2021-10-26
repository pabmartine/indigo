package com.martinia.indigo.ports.in.rest;

import java.util.List;
import java.util.Map;

import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;

public interface BookService {

	Long count(Search search);

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	String getCover(String path, boolean force);

	Book findById(String id);

	List<Book> getSimilar(List<String> similar);

	List<Book> getRecommendationsByBook(List<String> recommendations);

	List<Book> getRecommendationsByUser(String user);

	Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order);

	Long getNumSeries();

	List<Book> findBooksBySerie(String serie);

	Book findByPath(String path);

}
