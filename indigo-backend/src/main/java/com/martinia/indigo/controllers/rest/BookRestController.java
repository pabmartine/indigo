package com.martinia.indigo.controllers.rest;

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

import com.martinia.indigo.dto.BookDto;
import com.martinia.indigo.dto.Search;
import com.martinia.indigo.mappers.BookDtoMapper;
import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.model.indigo.FavoriteBook;
import com.martinia.indigo.model.indigo.MyBook;
import com.martinia.indigo.services.calibre.BookService;
import com.martinia.indigo.services.indigo.FavoriteBookService;
import com.martinia.indigo.services.indigo.MyBookService;
import com.martinia.indigo.utils.CoverComponent;

@RestController
@RequestMapping("/rest/book")
public class BookRestController {

	@Autowired
	private BookService bookService;

	@Autowired
	private FavoriteBookService favoriteBookService;

	@Autowired
	private MyBookService myBookService;

	@Autowired
	private CoverComponent coverComponent;

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
		List<BookDto> booksDto = bookDtoMapper.booksToBookDtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);
	}

	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getCover(@RequestParam String path, boolean force) throws IOException {
		Map<String, String> map = null;

		String image = coverComponent.getCover(path, force);

		map = new HashMap<String, String>();
		map.put("image", image);

		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> getBookInfoBy(@RequestParam int id, @RequestParam boolean local) {

		MyBook myBook = myBookService.getBookInfoBy(id, local);
		BookDto bookDto = bookDtoMapper.myBookToBookDto(myBook);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);

	}

	@GetMapping(value = "/title", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> getBookTitle(@RequestParam int id) {

		Book book = bookService.findById(id)
				.orElse(null);
		BookDto bookDto = bookDtoMapper.bookToBookDto(book);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

	@PostMapping(value = "/similar", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getSimilar(@RequestBody String similar) {

		List<Book> books = bookService.getSimilar(similar);
		List<BookDto> booksDto = bookDtoMapper.booksToBookDtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

	@GetMapping(value = "/recommendations/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getBookRecommendationsByBook(@RequestParam int id) {
		List<Book> books = bookService.getBookRecommendationsByBook(id);
		List<BookDto> booksDto = bookDtoMapper.booksToBookDtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}
	
	@GetMapping(value = "/recommendations/user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getBookRecommendationsByUser(@RequestParam int user) {
		List<Book> books = bookService.getBookRecommendationsByUser(user);
		List<BookDto> booksDto = bookDtoMapper.booksToBookDtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getFavoriteBooks(@RequestParam int user) {

		List<Book> books = myBookService.getFavoriteBooks(user);
		List<BookDto> booksDto = bookDtoMapper.booksToBookDtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> getFavoriteBook(@RequestParam int book, @RequestParam int user) {
		Boolean isFavorite = favoriteBookService.getFavoriteBook(book, user) != null;
		return new ResponseEntity<>(isFavorite, HttpStatus.OK);
	}

	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> addFavoriteBooks(@RequestParam int book, @RequestParam int user) {
		favoriteBookService.save(new FavoriteBook(user, book));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Transactional
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deleteFavoriteBooks(@RequestParam int book, @RequestParam int user) {
		favoriteBookService.deleteFavoriteBooks(book, user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/sent", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getSentBooks(@RequestParam int user) {

		List<Book> books = myBookService.getSentBooks(user);
		List<BookDto> booksDto = bookDtoMapper.booksToBookDtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

}
