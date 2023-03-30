package com.martinia.indigo.adapters.in.rest.controllers;

import com.martinia.indigo.adapters.in.rest.dtos.AuthorDto;
import com.martinia.indigo.adapters.in.rest.dtos.BookDto;
import com.martinia.indigo.adapters.in.rest.dtos.inner.ReviewDto;
import com.martinia.indigo.adapters.in.rest.mappers.AuthorDtoMapper;
import com.martinia.indigo.adapters.in.rest.mappers.BookDtoMapper;
import com.martinia.indigo.adapters.in.rest.mappers.ReviewDtoMapper;
import com.martinia.indigo.ports.in.rest.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/metadata")
public class MetadataRestController {

	@Autowired
	private MetadataService metadataService;

	@Autowired
	protected AuthorDtoMapper authorDtoMapper;

	@Autowired
	protected BookDtoMapper bookDtoMapper;

	@Autowired
	protected ReviewDtoMapper reviewDtoMapper;

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
		return new ResponseEntity<>(metadataService.getStatus(), HttpStatus.OK);
	}

	@GetMapping(value = "/author", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthorDto> refreshAuthor(@RequestParam String lang, @RequestParam String author) {
		AuthorDto authorDto = metadataService.findAuthorMetadata(author, lang).map(_author -> authorDtoMapper.domain2Dto(_author))
				.orElse(null);
		return new ResponseEntity<>(authorDto, HttpStatus.OK);
	}

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> refreshBook(@RequestParam String book) {
		BookDto bookDto = metadataService.findBookMetadata(book).map(_book -> bookDtoMapper.domain2Dto(_book)).orElse(null);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

	@GetMapping(value = "/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ReviewDto>> getReviews(@RequestParam String book) {
		List<ReviewDto> reviews = metadataService.getReviews(book).stream().map(review -> reviewDtoMapper.domain2Dto(review))
				.collect(Collectors.toList());
		return new ResponseEntity<>(reviews, HttpStatus.OK);
	}
}
