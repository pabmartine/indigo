package com.martinia.indigo.file.infrastructure.mongo.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.file.Path;
import java.util.UUID;

@Getter
@Setter
@Builder
@Document(collection = "files")
public class FileMongoEntity {

    @Id
    private UUID id;
    private String path;

}
