package com.martinia.indigo.shared.image;

import com.martinia.indigo.metadata.domain.ImageTagMetadata;

import java.io.IOException;
import java.nio.file.Path;

public interface ImageTagMetadataExtractor {
  ImageTagMetadata extract(Path path) throws IOException;
}
