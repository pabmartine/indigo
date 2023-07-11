package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMongoRepository extends MongoRepository<UserMongoEntity, String> {

	Optional<UserMongoEntity> findByUsername(String username);
}