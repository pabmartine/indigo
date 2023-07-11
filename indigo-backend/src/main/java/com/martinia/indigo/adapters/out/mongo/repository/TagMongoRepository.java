package com.martinia.indigo.adapters.out.mongo.repository;

import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomTagMongoRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagMongoRepository extends MongoRepository<TagMongoEntity, String>, CustomTagMongoRepository {

	@Query("{ 'name' : ?0 }")
	Optional<TagMongoEntity> findByName(String tag);
}