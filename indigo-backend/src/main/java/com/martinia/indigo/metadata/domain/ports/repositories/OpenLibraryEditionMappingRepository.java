package com.martinia.indigo.metadata.domain.ports.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryEditionMappingMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OpenLibraryEditionMappingRepository
		extends MongoRepository<OpenLibraryEditionMappingMongoEntity, String> {
	List<OpenLibraryEditionMappingMongoEntity> findByIndexVersion(String indexVersion);

	Optional<OpenLibraryEditionMappingMongoEntity> findFirstByIndexVersionAndIsbnIn(String indexVersion,
			Collection<String> isbns);

	long deleteByIndexVersion(String indexVersion);
}
