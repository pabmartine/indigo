package com.martinia.indigo.adapters.out.mongo.repository.custom;

import org.springframework.stereotype.Repository;

@Repository
public interface CustomGlobalMongoRepository {

	void dropCollection(Class<?> clazz);
}
