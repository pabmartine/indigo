package com.martinia.indigo.author.domain.ports.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.martinia.indigo.author.domain.model.AuthorPageData;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;

public interface CustomAuthorRepository {

	long count(List<String> languages);

	List<AuthorMongoEntity> findAll(List<String> languages, Pageable page);

	List<AuthorMongoEntity> findSummary(List<String> languages, Pageable page);

	AuthorPageData findSummaryPage(List<String> languages, Pageable page);
}

