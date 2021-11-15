package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.mongo.entities.AuthorMongoEntity;

@Repository
public interface CustomAuthorMongoRepository {

	long count(List<String> languages);

	List<AuthorMongoEntity> findAll(List<String> languages, Pageable page);
}
