package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.adapters.out.mongo.entities.UserMongoEntity;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<UserMongoEntity, String> {

	Optional<UserMongoEntity> findByUsername(String username);
}