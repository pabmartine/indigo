package com.martinia.indigo.file.infrastructure.mongo;

import com.martinia.indigo.file.domain.FileRepository;
import com.martinia.indigo.file.domain.model.File;
import com.martinia.indigo.file.infrastructure.mongo.entities.FileMongoEntity;
import com.martinia.indigo.file.infrastructure.mongo.repositories.FileMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

    private final FileMongoRepository fileMongoRepository;

    @Override
    public void deleteById(UUID id) {
        fileMongoRepository.deleteById(id);
    }

    @Override
    public Optional<File> findByPath(Path path) {
        return fileMongoRepository.findByPath(path.toString())
                .map(this::toDomain);
    }

    @Override
    public void save(File file) {
        fileMongoRepository.save(toEntity(file));
    }

    private File toDomain(FileMongoEntity entity) {
        return File.builder()
                .id(entity.getId())
                .path(Path.of(entity.getPath()))
                .build();
    }

    private FileMongoEntity toEntity(File domain) {
        return FileMongoEntity.builder()
                .id(domain.getId())
                .path(domain.getPath().toString())
                .build();
    }
}
