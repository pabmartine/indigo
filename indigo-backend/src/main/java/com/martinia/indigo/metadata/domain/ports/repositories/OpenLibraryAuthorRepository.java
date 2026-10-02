package com.martinia.indigo.metadata.domain.ports.repositories;

import java.util.List;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryAuthorMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OpenLibraryAuthorRepository extends MongoRepository<OpenLibraryAuthorMongoEntity, String> {
	List<OpenLibraryAuthorMongoEntity> findTop2ByIndexVersionAndNames(String indexVersion, String name);
	long deleteByIndexVersion(String indexVersion);
}
