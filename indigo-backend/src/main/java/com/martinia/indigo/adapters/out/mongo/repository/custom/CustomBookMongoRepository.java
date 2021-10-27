package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.domain.model.Search;

@Repository
public interface CustomBookMongoRepository {

	List<BookMongoEntity> findAll(Search search, int page, int size, String sort, String order);

	long count(Search search);

	Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order);

	Long getNumSeries();

	List<BookMongoEntity> getSimilar(List<String> similar);

	List<BookMongoEntity> getRecommendationsByBook(String id);
	
	List<BookMongoEntity> getRecommendationsByUser(String user, int page, int size, String sort, String order);

	List<BookMongoEntity> getRecommendationsByBook(List<String> recommendations, int num);

	Long countRecommendationsByUser(String user);

}