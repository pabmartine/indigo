package com.martinia.indigo.controllers.rest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.martinia.indigo.dto.Search;
import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.model.indigo.FavoriteBook;
import com.martinia.indigo.model.indigo.MyBook;
import com.martinia.indigo.services.calibre.BookService;
import com.martinia.indigo.services.indigo.FavoriteBookService;
import com.martinia.indigo.services.indigo.MyBookService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/book")
public class BookRestController {

	@Autowired
	private BookService bookService;

	@Autowired
	private FavoriteBookService favoriteBookService;

	@Autowired
	private MyBookService myBookService;

	@Value("${book.library.path}")
	private String libraryPath;

	@PostMapping(value = "/count/search/advance", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Long> getTotalAdvSearch(@RequestBody(required = false) Search search) {
		return new ResponseEntity<>(bookService.count(search), HttpStatus.OK);
	}

	// TODO MAPPING
	@PostMapping(value = "/all/advance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Book>> getBooks(@RequestBody(required = false) Search search, @RequestParam int page,
			@RequestParam int size, @RequestParam String sort, @RequestParam String order) {
		return new ResponseEntity<>(bookService.findAll(search, page, size, sort, order), HttpStatus.OK);
	}

	// TODO Bajar a servicio?
	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getCover(@RequestParam String path, boolean force) {
		Map<String, String> map = null;

		if (!libraryPath.endsWith(File.separator))
			libraryPath += File.separator;

		String coverPath = libraryPath + path + "/cover.jpg";
		String thumbPath = libraryPath + path + "/thumbails.jpg";

		try {
			File thumbFile = new File(thumbPath);

			if (!thumbFile.exists() || force) {

				File coverFile = new File(coverPath);

				BufferedImage i = ImageIO.read(coverFile);
				BufferedImage scaledImg = Scalr.resize(i, 250);

				ImageIO.write(scaledImg, "JPG", thumbFile);
			}

			String image = Base64.getEncoder()
					.encodeToString(Files.readAllBytes(thumbFile.toPath()));

			map = new HashMap<String, String>();
			map.put("image", image);
		} catch (IOException e) {
			log.error("Path " + thumbPath + " not exist");

		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	// TODO MAPPING
	@GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MyBook> getBookInfoBy(@RequestParam int id, @RequestParam boolean local) {
		return new ResponseEntity<>(myBookService.getBookInfoBy(id, local), HttpStatus.OK);
	}

	// TODO MAPPING
	@GetMapping(value = "/title", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Book> getBookTitle(@RequestParam int id) {
		return new ResponseEntity<>(bookService.findById(id)
				.get(), HttpStatus.OK);
	}

	// TODO MAPPING
	@PostMapping(value = "/similar", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Book>> getSimilar(@RequestBody String similar) {
		return new ResponseEntity<>(bookService.getSimilar(similar), HttpStatus.OK);
	}

	// TODO MAPPING
	@GetMapping(value = "/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Book>> getBookRecommendations(@RequestParam int id) {
		return new ResponseEntity<>(bookService.getBookRecommendations(id), HttpStatus.OK);
	}

	// TODO Bajar a servicio?
	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Book>> getFavoriteBooks(@RequestParam int user) {
		List<Book> books = new ArrayList<Book>();

		List<Integer> data = myBookService.getFavoriteBooks(user);
		for (Integer id : data) {
			books.add(bookService.findById(id)
					.get());
		}

		return new ResponseEntity<>(books, HttpStatus.OK);

	}

	// TODO MAPPING
	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FavoriteBook> getFavoriteBook(@RequestParam int book, @RequestParam int user) {
		FavoriteBook fb = favoriteBookService.getFavoriteBook(book, user);
		return new ResponseEntity<>(fb, HttpStatus.OK);
	}

	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> addFavoriteBooks(@RequestParam int book, @RequestParam int user) {
		favoriteBookService.save(new FavoriteBook(user, book));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// TODO Bajar a servicio?
	@Transactional
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deleteFavoriteBooks(@RequestParam int book, @RequestParam int user) {
		FavoriteBook fb = favoriteBookService.getFavoriteBook(book, user);
		favoriteBookService.delete(fb);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// TODO Bajar a servicio?
	// TODO MAPPING
	@GetMapping(value = "/sent", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Book>> getSentBooks(@RequestParam int user) {
		List<Book> list = new ArrayList<Book>();

		List<Integer> data = myBookService.getSentBooks(user);
		for (Integer id : data) {
			list.add(bookService.findById(id)
					.get());
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

}
