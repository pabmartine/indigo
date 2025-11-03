package com.martinia.indigo.shared.image;

import com.martinia.indigo.metadata.domain.ImageTagMetadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ImageTagMetadataExtractorImpl implements ImageTagMetadataExtractor {

  @Override
  public ImageTagMetadata extract(Path path) throws IOException {
    try {
      Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());

      JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
      ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

      String format = jpegDirectory != null ? jpegDirectory.getName() : "unknown";
      int width = jpegDirectory != null ? jpegDirectory.getImageWidth() : 0;
      int height = jpegDirectory != null ? jpegDirectory.getImageHeight() : 0;

      List<String> tags = new ArrayList<>();
      if (exifIFD0Directory != null) {
        for (Tag tag : exifIFD0Directory.getTags()) {
          tags.add(tag.getTagName() + ": " + tag.getDescription());
        }
      }

      return ImageTagMetadata.builder()
          .format(format)
          .width(width)
          .height(height)
          .tags(tags)
          .build();
    } catch (ImageProcessingException | MetadataException | IOException e) {
      return ImageTagMetadata.builder()
          .format("unknown")
          .width(0)
          .height(0)
          .tags(Collections.emptyList())
          .build();
    }
  }
}
