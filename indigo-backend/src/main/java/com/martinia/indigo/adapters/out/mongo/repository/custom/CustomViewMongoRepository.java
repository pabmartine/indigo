package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;

@Repository
public interface CustomViewMongoRepository {

	List<BookMongoEntity> getRecommendations(String user, int num);
}
