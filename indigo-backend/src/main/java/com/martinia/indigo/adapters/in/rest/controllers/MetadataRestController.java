package com.martinia.indigo.adapters.in.rest.controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.martinia.indigo.adapters.in.rest.dtos.AuthorDto;
import com.martinia.indigo.adapters.in.rest.dtos.BookDto;
import com.martinia.indigo.adapters.in.rest.mappers.AuthorDtoMapper;
import com.martinia.indigo.adapters.in.rest.mappers.BookDtoMapper;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.ports.in.rest.MetadataService;

@RestController
@RequestMapping("/rest/metadata")
public class MetadataRestController {

  @Autowired
  private MetadataService metadataService;

  @Autowired
  protected AuthorDtoMapper authorDtoMapper;

  @Autowired
  protected BookDtoMapper bookDtoMapper;

  @GetMapping(value = "/start")
  public ResponseEntity<Void> initialLoad(@RequestParam String lang, @RequestParam String type, @RequestParam String entity) {
    metadataService.start(lang, type, entity);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping(value = "/stop")
  public ResponseEntity<Void> stop() {
    metadataService.stop();
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> getStatus() {
    Map<String, Object> ret = null;
    ret = metadataService.getStatus();
    return new ResponseEntity<>(ret, HttpStatus.OK);
  }

  @GetMapping(value = "/author", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthorDto> refreshAuthor(@RequestParam String lang, @RequestParam String author) {
    Author _author = metadataService.findAuthorMetadata(author, lang);
    AuthorDto authorDto = authorDtoMapper.domain2Dto(_author);
    return new ResponseEntity<>(authorDto, HttpStatus.OK);
  }

  @GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BookDto> refreshBook(@RequestParam String book) {
    Book _book = metadataService.findBookMetadata(book);
    BookDto bookDto = bookDtoMapper.domain2Dto(_book);
    return new ResponseEntity<>(bookDto, HttpStatus.OK);
  }
}
