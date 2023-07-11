package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.common.infrastructure.mongo.entities.ViewMongoEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewMongoRepository extends MongoRepository<ViewMongoEntity, String> {

}