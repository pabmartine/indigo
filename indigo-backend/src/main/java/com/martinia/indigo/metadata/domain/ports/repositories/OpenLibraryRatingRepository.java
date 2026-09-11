package com.martinia.indigo.metadata.domain.ports.repositories;

import java.util.Optional;

import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryRatingMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OpenLibraryRatingRepository extends MongoRepository<OpenLibraryRatingMongoEntity, String> {
	Optional<OpenLibraryRatingMongoEntity> findByIndexVersionAndWorkId(String indexVersion, String workId);

	long deleteByIndexVersion(String indexVersion);
}
