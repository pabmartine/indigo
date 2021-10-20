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

	List<BookMongoEntity> getBookRecommendations(String id, int page, int size, String sort, String order);

	Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order);

	Long getNumSeries();

}
