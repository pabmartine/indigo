package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.metadata.domain.ImageTagMetadata;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

public interface FindImageTagMetadataUseCase {

  FindImageTagMetadataResult findImageTagMetadata(FindImageTagMetadataCommand command);

  @Value
  @Builder
  static class FindImageTagMetadataCommand {
    @NotBlank String imagePath;

    public FindImageTagMetadataCommand(String imagePath) {
      this.imagePath = imagePath;
    }
  }

  @Value
  @Builder
  static class FindImageTagMetadataResult {
    ImageTagMetadata imageTagMetadata;
  }
}
