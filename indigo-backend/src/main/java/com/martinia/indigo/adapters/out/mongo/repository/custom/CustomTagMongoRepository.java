package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.mongo.entities.TagMongoEntity;

@Repository
public interface CustomTagMongoRepository {

	List<TagMongoEntity> findAll(List<String> languages, Sort sort);
}
