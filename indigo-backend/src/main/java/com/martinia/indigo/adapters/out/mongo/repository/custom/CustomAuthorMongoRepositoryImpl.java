package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.martinia.indigo.adapters.out.mongo.entities.AuthorMongoEntity;

public class CustomAuthorMongoRepositoryImpl implements CustomAuthorMongoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public long count() {

		Query query = new Query();

		return mongoTemplate.count(query, AuthorMongoEntity.class);

	}

}
