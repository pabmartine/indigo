package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.martinia.indigo.adapters.out.mongo.entities.TagMongoEntity;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomGlobalMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomTagMongoRepository;

import java.util.Optional;

public interface TagMongoRepository extends MongoRepository<TagMongoEntity, String>, CustomTagMongoRepository, CustomGlobalMongoRepository {

	@Query("{ 'name' : ?0 }")
	Optional<TagMongoEntity> findByName(String tag);
}