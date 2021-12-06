package com.martinia.indigo.ports.in.rest;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;

public interface BookService {

	Long count(Search search);

	List<Book> findAll(Search search, int page, int size, String sort, String order);

	Book findById(String id);

	List<Book> getSimilar(List<String> similar, List<String> languages);

	List<Book> getRecommendationsByBook(List<String> recommendations, List<String> languages);

	List<Book> getRecommendationsByUser(String user, int page, int size, String sort, String order);

	Map<String, Long> getNumBooksBySerie(List<String> languages, int page, int size, String sort, String order);

	Long getNumSeries(List<String> languages);

	List<Book> findBooksBySerie(String serie);

	Book findByPath(String path);

	Long countRecommendationsByUser(String user);

	Resource getEpub(String path);

	List<String> getBookLanguages();

}
