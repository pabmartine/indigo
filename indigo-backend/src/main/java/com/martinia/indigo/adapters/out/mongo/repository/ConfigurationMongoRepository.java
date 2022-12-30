package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.adapters.out.mongo.entities.ConfigurationMongoEntity;

import java.util.Optional;

public interface ConfigurationMongoRepository extends MongoRepository<ConfigurationMongoEntity, String> {

	Optional<ConfigurationMongoEntity> findByKey(String string);
}