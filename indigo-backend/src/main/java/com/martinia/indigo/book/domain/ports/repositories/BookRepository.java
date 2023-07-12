package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<BookMongoEntity, String>, CustomBookRepository {

	@Query("{ 'serie.name' : ?0 }")
	List<BookMongoEntity> findBooksBySerie(String serie);

	@Query("{ 'tags' : ?0 }")
	List<BookMongoEntity> findByTag(String name);

	@Query("{ 'title' : ?0 }")
	Optional<BookMongoEntity> findByTitle(String title);

	@Query("{ 'path' : ?0 }")
	Optional<BookMongoEntity> findByPath(String path);

}