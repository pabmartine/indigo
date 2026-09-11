package com.martinia.indigo.metadata.domain.model;

import java.nio.file.Path;

public record OpenLibraryDownloadResult(Path path, long size, String lastModified, String etag) {
}
