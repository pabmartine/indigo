package com.martinia.indigo.adapters.out.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomBookMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomGlobalMongoRepository;

public interface BookMongoRepository
		extends MongoRepository<BookMongoEntity, String>, CustomBookMongoRepository, CustomGlobalMongoRepository {

	@Query("{ 'serie.name' : ?0 }")
	List<BookMongoEntity> findBooksBySerie(String serie);

	@Query("{ 'tags' : ?0 }")
	List<BookMongoEntity> findByTag(String name);

	@Query("{ 'title' : ?0 }")
	Optional<BookMongoEntity> findByTitle(String title);

	@Query("{ 'path' : ?0 }")
	Optional<BookMongoEntity> findByPath(String path);

//	BookMongoEntity findFirstByReviewsLastMetadataSyncExistsTrue();
	

}