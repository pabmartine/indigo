package com.martinia.indigo.ports.in.rest;

import java.util.Map;
import com.martinia.indigo.domain.model.Author;

public interface MetadataService {

  Map<String, Object> getStatus();

  void stop();

  void start(String lang, String type, String entity);

  Author findAuthorMetadata(String sort, String lang);

}
