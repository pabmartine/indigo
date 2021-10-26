package com.martinia.indigo.adapters.out.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.adapters.out.mongo.entities.ViewMongoEntity;
import com.martinia.indigo.adapters.out.mongo.repository.custom.CustomViewMongoRepository;

public interface ViewMongoRepository extends MongoRepository<ViewMongoEntity, String>, CustomViewMongoRepository {

}