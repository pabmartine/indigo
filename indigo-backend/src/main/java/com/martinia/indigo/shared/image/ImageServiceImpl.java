package com.martinia.indigo.shared.image;

import com.martinia.indigo.metadata.domain.ImageTagMetadata;
import com.martinia.indigo.shared.file.FileService;
import com.martinia.indigo.shared.image.ImageService;
import com.martinia.indigo.shared.image.ImageTagMetadataExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final ImageTagMetadataExtractor imageTagMetadataExtractor;

  @Override
  public ImageTagMetadata getImageTagMetadata(Path path) throws IOException {
    return imageTagMetadataExtractor.extract(path);
  }
}
