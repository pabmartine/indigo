package com.martinia.indigo.file.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.UUID;

@Getter
@Setter
@Builder
public class File {

    private UUID id;
    private Path path;

}
