package com.martinia.indigo.metadata.domain.ports.repositories;

import java.util.Optional;

import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryIndexVersionMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OpenLibraryIndexVersionRepository
		extends MongoRepository<OpenLibraryIndexVersionMongoEntity, String> {
	Optional<OpenLibraryIndexVersionMongoEntity> findFirstByActiveTrue();
}
