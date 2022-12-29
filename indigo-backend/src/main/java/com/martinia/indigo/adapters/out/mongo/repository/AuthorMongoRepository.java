package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.martinia.indigo.adapters.out.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomAuthorMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomGlobalMongoRepository;

import java.util.Optional;

public interface AuthorMongoRepository
		extends MongoRepository<AuthorMongoEntity, String>, CustomAuthorMongoRepository, CustomGlobalMongoRepository {

	@Query("{ 'name' : ?0 }")
	Optional<AuthorMongoEntity> findByName(String name);

	@Query("{ 'sort' : ?0 }")
	Optional<AuthorMongoEntity> findBySort(String sort);

}