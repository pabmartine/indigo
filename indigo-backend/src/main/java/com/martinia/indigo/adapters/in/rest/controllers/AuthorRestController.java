package com.martinia.indigo.adapters.in.rest.controllers;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.adapters.in.rest.dtos.AuthorDto;
import com.martinia.indigo.adapters.in.rest.mappers.AuthorDtoMapper;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.ports.in.rest.AuthorService;
import com.martinia.indigo.ports.in.rest.UserService;

@RestController
@RequestMapping("/rest/author")
public class AuthorRestController {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthorService authorService;

	@Autowired
	protected AuthorDtoMapper authorDtoMapper;

	@GetMapping("/count")
	public ResponseEntity<Long> count() {
		return new ResponseEntity<>(authorService.count(), HttpStatus.OK);
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AuthorDto>> getAll(@RequestParam int page, @RequestParam int size,
			@RequestParam String sort, @RequestParam String order) {
		List<Author> authors = authorService.findAll(page, size, sort, order);
		List<AuthorDto> authorsDto = authorDtoMapper.domains2Dtos(authors);
		return new ResponseEntity<>(authorsDto, HttpStatus.OK);
	}

	@GetMapping(value = "/sort", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthorDto> findBySortName(@RequestParam String sort) {
		Author author = authorService.findBySort(sort);
		AuthorDto authorDto = authorDtoMapper.domain2Dto(author);
		return new ResponseEntity<>(authorDto, HttpStatus.OK);
	}

	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AuthorDto>> getFavoriteAuthors(@RequestParam String user) {
		List<Author> authors = userService.getFavoriteAuthors(user);
		List<AuthorDto> authorsDto = authorDtoMapper.domains2Dtos(authors);
		return new ResponseEntity<>(authorsDto, HttpStatus.OK);
	}

	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> getFavoriteAuthor(@RequestParam String author, @RequestParam String user) {
		Boolean isFavorite = userService.isFavoriteAuthor(user, String.valueOf(author));
		return new ResponseEntity<>(isFavorite, HttpStatus.OK);
	}

	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> addFavoriteAuthors(@RequestParam String author, @RequestParam String user) {
		userService.addFavoriteAuthor(user, String.valueOf(author));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Transactional
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deleteFavoriteAuthors(@RequestParam String author, @RequestParam String user) {
		userService.deleteFavoriteAuthor(user, String.valueOf(author));
		return new ResponseEntity<>(HttpStatus.OK);

	}
}
