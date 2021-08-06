package com.martinia.indigo.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import org.imgscalr.Scalr;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
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
import com.martinia.indigo.repository.calibre.BookRepository;
import com.martinia.indigo.repository.indigo.ConfigurationRepository;
import com.martinia.indigo.repository.indigo.FavoriteBookRepository;
import com.martinia.indigo.repository.indigo.MyBookRepository;
import com.martinia.indigo.services.GoodReadsService;
import com.martinia.indigo.services.GoogleBooksService;

@RestController
@RequestMapping("/rest/book")
public class BookRestController {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BookRestController.class);

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private FavoriteBookRepository favoriteBookRepository;

	@Autowired
	private MyBookRepository myBookRepository;

	@Autowired
	private GoodReadsService goodReadsService;

	@Autowired
	private GoogleBooksService googleBooksService;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Value("${book.library.path}")
	private String libraryPath;

	@PostMapping(value = "/count/search/advance", consumes = MediaType.APPLICATION_JSON_VALUE)
	public long getTotalAdvSearch(@RequestBody(required = false) Search search) {
		return bookRepository.count(search);
	}

	@PostMapping(value = "/all/advance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Book> getBooks(@RequestBody(required = false) Search search, @RequestParam int page,
			@RequestParam int size, @RequestParam String sort, @RequestParam String order) {
		return bookRepository.findAll(search, page, size, sort, order);
	}


	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> getCover(@RequestParam String path, boolean force) {
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

			String image = Base64.getEncoder().encodeToString(Files.readAllBytes(thumbFile.toPath()));

			map = new HashMap<String, String>();
			map.put("image", image);
		} catch (IOException e) {
			LOG.error("Path " + thumbPath + " not exist");

		}

		return map;
	}

	@GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public MyBook getBookInfoBy(@RequestParam int id, @RequestParam boolean local) {

		MyBook myBook = null;

		Optional<MyBook> optional = myBookRepository.findById(id);

		if (!optional.isPresent()) {
			if (!local) {
				Book book = bookRepository.findById(id).get();

				myBook = goodReadsService.findBook(book.getTitle(), book.getAuthorSort());

				if (myBook == null) {
					myBook = googleBooksService.findBook(book.getTitle(), book.getAuthorSort());
				}

				if (myBook != null) {
					myBook.setId(book.getId());
					myBookRepository.save(myBook);
				}

			}
		} else {
			myBook = optional.get();
		}

		return myBook;
	}

	@GetMapping(value = "/title", produces = MediaType.APPLICATION_JSON_VALUE)
	public Book getBookTitle(@RequestParam int id) {
		return bookRepository.findById(id).get();
	}

	@PostMapping(value = "/similar", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Book> getSimilar(@RequestBody String similar) {
		List<Book> list = new ArrayList<Book>();

		String[] data = similar.split(";");
		for (String id : data) {
			int bookId = Integer.parseInt(id);
			Book book = bookRepository.findById(bookId).get();
			list.add(book);
		}
		return list;
	}

	@GetMapping(value = "/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Book> getBookRecommendations(@RequestParam int id) {
		int max = Integer.parseInt(configurationRepository.findById("books.recommendations").get().getValue());
		return bookRepository.getBookRecommendations(id, PageRequest.of(0, max, Sort.by("id")));
	}

	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Book> getFavoriteBooks(@RequestParam int user) {
		List<Book> list = new ArrayList<Book>();

		List<Integer> data = myBookRepository.getFavoriteBooks(user);
		for (Integer id : data) {
			list.add(bookRepository.findById(id).get());
		}

		return list;
	}

	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public FavoriteBook getFavoriteBook(@RequestParam int book, @RequestParam int user) {
		FavoriteBook fb = favoriteBookRepository.getFavoriteBook(book, user);
		return fb;
	}

	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public void addFavoriteBooks(@RequestParam int book, @RequestParam int user) {
		FavoriteBook fb = new FavoriteBook(user, book);
		favoriteBookRepository.save(fb);
	}

	@Transactional
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteFavoriteBooks(@RequestParam int book, @RequestParam int user) {
		FavoriteBook fb = favoriteBookRepository.getFavoriteBook(book, user);
		favoriteBookRepository.delete(fb);
	}

	@GetMapping(value = "/sent", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Book> getSentBooks(@RequestParam int user) {
		List<Book> list = new ArrayList<Book>();

		List<Integer> data = myBookRepository.getSentBooks(user);
		for (Integer id : data) {
			list.add(bookRepository.findById(id).get());
		}

		return list;
	}

}
