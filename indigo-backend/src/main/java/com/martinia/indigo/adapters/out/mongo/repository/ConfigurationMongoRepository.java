package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.adapters.out.mongo.entities.ConfigurationMongoEntity;

public interface ConfigurationMongoRepository extends MongoRepository<ConfigurationMongoEntity, String> {

	ConfigurationMongoEntity findByKey(String string);
}