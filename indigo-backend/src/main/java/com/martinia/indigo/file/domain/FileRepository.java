package com.martinia.indigo.file.domain;

import com.martinia.indigo.file.domain.model.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository {

    void deleteById(UUID id);
    Optional<File> findByPath(Path path);
    void save(File file);

}
