package com.martinia.indigo.user.domain.ports.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserMongoEntity, String> {

	Optional<UserMongoEntity> findByUsername(String username);
}