package com.martinia.indigo.file.infrastructure.mongo.repositories;

import com.martinia.indigo.file.infrastructure.mongo.entities.FileMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMongoRepository extends MongoRepository<FileMongoEntity, UUID> {

    Optional<FileMongoEntity> findByPath(String path);

}
