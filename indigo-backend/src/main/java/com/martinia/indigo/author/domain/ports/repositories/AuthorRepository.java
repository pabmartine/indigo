package com.martinia.indigo.author.domain.ports.repositories;

import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends MongoRepository<AuthorMongoEntity, String>, CustomAuthorRepository {

	@Query("{ 'name' : ?0 }")
	Optional<AuthorMongoEntity> findByName(String name);
}