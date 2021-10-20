package com.martinia.indigo.adapters.out.mongo.repository.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public class CustomGlobalMongoRepositoryImpl implements CustomGlobalMongoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	public void dropCollection(Class<?> clazz) {
		mongoTemplate.remove(new Query(), clazz);
	}

}
