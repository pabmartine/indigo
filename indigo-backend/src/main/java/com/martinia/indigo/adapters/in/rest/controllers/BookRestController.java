package com.martinia.indigo.adapters.in.rest.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.adapters.in.rest.dtos.BookDto;
import com.martinia.indigo.adapters.in.rest.mappers.BookDtoMapper;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;
import com.martinia.indigo.domain.model.View;
import com.martinia.indigo.ports.in.rest.BookService;
import com.martinia.indigo.ports.in.rest.NotificationService;
import com.martinia.indigo.ports.in.rest.UserService;
import com.martinia.indigo.ports.in.rest.ViewService;

@RestController
@RequestMapping("/rest/book")
public class BookRestController {

	@Autowired
	private BookService bookService;

	@Autowired
	private UserService userService;

	@Autowired
	private ViewService viewService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	protected BookDtoMapper bookDtoMapper;

	@PostMapping(value = "/count/search/advance", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Long> getTotalAdvSearch(@RequestBody(required = false) Search search) {
		return new ResponseEntity<>(bookService.count(search), HttpStatus.OK);
	}

	@PostMapping(value = "/all/advance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getBooks(@RequestBody(required = false) Search search, @RequestParam int page,
			@RequestParam int size, @RequestParam String sort, @RequestParam String order) {
		List<Book> books = bookService.findAll(search, page, size, sort, order);
		List<BookDto> booksDto = bookDtoMapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);
	}

	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getCover(@RequestParam String path, boolean force) throws IOException {
		Map<String, String> map = null;

		String image = bookService.getCover(path.replace("@_@", "&")
				.replace("@-@", "[")
				.replace("@¡@", "]"), force);

		map = new HashMap<String, String>();
		map.put("image", image);

		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@GetMapping(value = "/id", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> getBookById(@RequestParam String id) {

		Book book = bookService.findById(id);
		BookDto bookDto = bookDtoMapper.domain2Dto(book);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

	@GetMapping(value = "/path", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> getBookByPath(@RequestParam String path) {

		Book book = bookService.findByPath(path);
		BookDto bookDto = bookDtoMapper.domain2Dto(book);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

	@PostMapping(value = "/similar", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getSimilar(@RequestBody List<String> similar) {
		List<Book> books = bookService.getSimilar(similar);
		List<BookDto> booksDto = bookDtoMapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

	@PostMapping(value = "/recommendations/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getBookRecommendationsByBook(@RequestBody List<String> recommendations) {
		List<Book> books = bookService.getRecommendationsByBook(recommendations);
		List<BookDto> booksDto = bookDtoMapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

	@GetMapping(value = "/recommendations/user/count")
	public ResponseEntity<Long> countBookRecommendationsByUser(@RequestParam String user) {
		return new ResponseEntity<>(bookService.countRecommendationsByUser(user), HttpStatus.OK);
	}

	@GetMapping(value = "/recommendations/user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getBookRecommendationsByUser(@RequestParam String user, @RequestParam int page,
			@RequestParam int size, @RequestParam String sort, @RequestParam String order) {
		List<Book> books = bookService.getRecommendationsByUser(user, page, size, sort, order);
		List<BookDto> booksDto = bookDtoMapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getFavoriteBooks(@RequestParam String user) {
		List<Book> books = userService.getFavoriteBooks(user);
		List<BookDto> booksDto = bookDtoMapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> getFavoriteBook(@RequestParam String book, @RequestParam String user) {
		Boolean isFavorite = userService.isFavoriteBook(user, book);
		return new ResponseEntity<>(isFavorite, HttpStatus.OK);
	}

	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> addFavoriteBooks(@RequestParam String book, @RequestParam String user) {
		userService.addFavoriteBook(user, book);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Transactional
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deleteFavoriteBooks(@RequestParam String book, @RequestParam String user) {
		userService.deleteFavoriteBook(user, book);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/sent", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getSentBooks(@RequestParam String user) {

		List<Book> books = notificationService.getSentBooks(user);
		List<BookDto> booksDto = bookDtoMapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

	@PostMapping(value = "/view", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> view(@RequestParam String book, @RequestParam String user) {
		viewService.save(new View(book, user));
		return new ResponseEntity<>(HttpStatus.OK);

	}

}