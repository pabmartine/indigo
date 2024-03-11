package com.martinia.indigo.configuration.domain.ports.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends MongoRepository<ConfigurationMongoEntity, String> {

	Optional<ConfigurationMongoEntity> findByKey(String string);
}