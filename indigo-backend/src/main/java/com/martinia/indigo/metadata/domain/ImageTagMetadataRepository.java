package com.martinia.indigo.metadata.domain;

public interface ImageTagMetadataRepository {
  ImageTagMetadata findByImagePath(String imagePath);
}
