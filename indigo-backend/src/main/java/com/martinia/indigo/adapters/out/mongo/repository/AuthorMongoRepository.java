package com.martinia.indigo.adapters.out.mongo.repository;

import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomAuthorMongoRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AuthorMongoRepository extends MongoRepository<AuthorMongoEntity, String>, CustomAuthorMongoRepository {

	@Query("{ 'name' : ?0 }")
	Optional<AuthorMongoEntity> findByName(String name);

	@Query("{ 'sort' : ?0 }")
	Optional<AuthorMongoEntity> findBySort(String sort);

}