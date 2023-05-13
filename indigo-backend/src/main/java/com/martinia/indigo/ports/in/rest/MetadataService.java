package com.martinia.indigo.ports.in.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.inner.Review;

public interface MetadataService {

  Map<String, Object> getStatus();

  void stop();

  void start(String lang, String type, String entity);

  Optional<Author> findAuthorMetadata(String sort, String lang);

  Optional<Book> findBookMetadata(String book);

//  List<Review> getReviews(String book);

}
