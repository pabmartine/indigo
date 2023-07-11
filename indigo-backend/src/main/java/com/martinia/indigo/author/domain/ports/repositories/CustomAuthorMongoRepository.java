package com.martinia.indigo.author.domain.ports.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;


public interface CustomAuthorMongoRepository {

	long count(List<String> languages);

	List<AuthorMongoEntity> findAll(List<String> languages, Pageable page);
}
