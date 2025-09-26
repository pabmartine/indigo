package com.martinia.indigo.metadata.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ImageTagMetadata {
  String format;
  int width;
  int height;
  List<String> tags;
}
