package com.martinia.indigo.metadata.adapter.web;

import com.martinia.indigo.metadata.application.commands.FindImageTagMetadataUseCase;
import com.martinia.indigo.metadata.application.commands.FindImageTagMetadataUseCase.FindImageTagMetadataCommand;
import com.martinia.indigo.metadata.application.commands.FindImageTagMetadataUseCase.FindImageTagMetadataResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metadata/image")
@RequiredArgsConstructor
public class ImageMetadataController {

  private final FindImageTagMetadataUseCase findImageTagMetadataUseCase;

  @GetMapping
  public ResponseEntity<FindImageTagMetadataResult> findImageTagMetadata(
      @RequestParam("imagePath") String imagePath) {
    FindImageTagMetadataCommand command = new FindImageTagMetadataCommand(imagePath);
    FindImageTagMetadataResult result = findImageTagMetadataUseCase.findImageTagMetadata(command);
    return ResponseEntity.ok(result);
  }
}
