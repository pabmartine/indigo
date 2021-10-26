package com.martinia.indigo.ports.out.mongo;

import java.util.List;
import java.util.Map;

import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;

public interface BookRepository {

	Long count(Search search);

	void dropCollection();

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	List<Book> findBooksBySerie(String serie);

	Book findById(String id);

	Book findByPath(String path);

	Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order);

	Long getNumSeries();

	List<Book> getRecommendationsByBook(List<String> recommendations, int num);

	List<Book> getRecommendationsByBook(String id);

	List<Book> getRecommendationsByUser(String user, int num);

	List<Book> getSimilar(List<String> similar);

	void save(Book book);

}
