package com.martinia.indigo.metadata.domain.ports.repositories;

import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryIndexJobMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OpenLibraryIndexJobRepository extends MongoRepository<OpenLibraryIndexJobMongoEntity, String> {
}
