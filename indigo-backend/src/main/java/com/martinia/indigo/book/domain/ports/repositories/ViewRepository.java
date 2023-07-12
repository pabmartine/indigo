package com.martinia.indigo.book.domain.ports.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.martinia.indigo.common.infrastructure.mongo.entities.ViewMongoEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewRepository extends MongoRepository<ViewMongoEntity, String> {

}