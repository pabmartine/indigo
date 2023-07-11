package com.martinia.indigo.book.domain.ports.repositories;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.model.Search;

public interface CustomBookMongoRepository {

	List<BookMongoEntity> findAll(Search search, int page, int size, String sort, String order);

	long count(Search search);

	Map<String, Long> getNumBooksBySerie(List<String> languages, int page, int size, String sort, String order);

	Long getNumSeries(List<String> languages);

	List<BookMongoEntity> getSimilar(List<String> similar, List<String> languages);

	List<BookMongoEntity> getSerie(String serie, List<String> languages);

	List<BookMongoEntity> getRecommendationsByBook(BookMongoEntity book);

	List<BookMongoEntity> getRecommendationsByUser(String user, int page, int size, String sort, String order);

	List<BookMongoEntity> getRecommendationsByBook(List<String> recommendations, List<String> languages, int num);

	long countRecommendationsByUser(String user);

	List<String> getBookLanguages();

}
