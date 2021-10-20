package com.martinia.indigo.ports.out.mongo;

import java.util.List;
import java.util.Map;

import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;

public interface BookRepository {

	Long count(Search search);

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	List<Book> findBooksBySerie(String serie);

	Book findById(String id);

	List<Book> getBookRecommendationsByBook(String id, int num);

	List<Book> getBookRecommendationsByUser(String user, int num);

	Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order);

	Long getNumSeries();

	List<Book> getSimilar(List<String> similar);

	void save(Book book);

	void dropCollection();

	Book findByPath(String path);

}
