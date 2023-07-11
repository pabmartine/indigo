package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationMongoRepository extends MongoRepository<ConfigurationMongoEntity, String> {

	Optional<ConfigurationMongoEntity> findByKey(String string);
}