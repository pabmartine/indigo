package com.martinia.indigo.tag.domain.ports.repositories;

import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends MongoRepository<TagMongoEntity, String>, CustomTagRepository {

	@Query("{ 'name' : ?0 }")
	Optional<TagMongoEntity> findByName(String tag);
}