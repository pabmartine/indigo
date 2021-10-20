package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.adapters.out.mongo.entities.UserMongoEntity;

public interface UserMongoRepository extends MongoRepository<UserMongoEntity, String> {

	UserMongoEntity findByUsername(String username);
}