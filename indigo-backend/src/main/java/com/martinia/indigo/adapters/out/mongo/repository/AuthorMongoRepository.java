package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.martinia.indigo.adapters.out.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomAuthorMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomGlobalMongoRepository;

public interface AuthorMongoRepository
		extends MongoRepository<AuthorMongoEntity, String>, CustomAuthorMongoRepository, CustomGlobalMongoRepository {

	@Query("{ 'name' : ?0 }")
	AuthorMongoEntity findByName(String name);

	@Query("{ 'sort' : ?0 }")
	AuthorMongoEntity findBySort(String sort);

}