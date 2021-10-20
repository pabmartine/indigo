package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.martinia.indigo.adapters.out.mongo.entities.TagMongoEntity;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomGlobalMongoRepository;

public interface TagMongoRepository extends MongoRepository<TagMongoEntity, String>, CustomGlobalMongoRepository {

	@Query("{ 'name' : ?0 }")
	TagMongoEntity findByName(String tag);
}