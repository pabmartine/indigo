package com.martinia.indigo.tag.domain.ports.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;


public interface CustomTagRepository {

	List<TagMongoEntity> findAll(List<String> languages, Sort sort);
}
