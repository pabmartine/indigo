package com.martinia.indigo.metadata.infrastructure;

import com.martinia.indigo.metadata.domain.ImageTagMetadata;
import com.martinia.indigo.metadata.domain.ImageTagMetadataRepository;
import com.martinia.indigo.shared.file.FileService;
import com.martinia.indigo.shared.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

@Repository
@RequiredArgsConstructor
public class ImageTagMetadataRepositoryImpl implements ImageTagMetadataRepository {

  private final FileService fileService;
  private final ImageService imageService;

  @Override
  public ImageTagMetadata findByImagePath(String imagePath) {
    Path path = fileService.getPath(imagePath);
    try {
      return imageService.getImageTagMetadata(path);
    } catch (IOException e) {
      return ImageTagMetadata.builder()
          .format("unknown")
          .width(0)
          .height(0)
          .tags(Collections.emptyList())
          .build();
    }
  }
}
