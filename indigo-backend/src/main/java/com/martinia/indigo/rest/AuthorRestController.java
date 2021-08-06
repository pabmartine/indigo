package com.martinia.indigo.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.model.calibre.Author;
import com.martinia.indigo.model.indigo.FavoriteAuthor;
import com.martinia.indigo.model.indigo.FavoriteBook;
import com.martinia.indigo.model.indigo.MyAuthor;
import com.martinia.indigo.repository.calibre.AuthorRepository;
import com.martinia.indigo.repository.indigo.FavoriteAuthorRepository;
import com.martinia.indigo.repository.indigo.MyAuthorRepository;
import com.martinia.indigo.services.AsyncService;
import com.martinia.indigo.services.GoodReadsService;
import com.martinia.indigo.services.WikipediaService;

@RestController
@RequestMapping("/rest/author")
public class AuthorRestController {

	@Autowired
	private FavoriteAuthorRepository favoriteAuthorRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private MyAuthorRepository myAuthorRepository;

	@Autowired
	private WikipediaService wikipediaService;

	@Autowired
	private GoodReadsService goodReadsService;

	@Autowired
	private AsyncService asyncService;

	@GetMapping("/count")
	public long getTotal() {
		return authorRepository.count();
	}

	@GetMapping(value = "/numbooks", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, String>> getNumBooksByAuthor(@RequestParam int page, @RequestParam int size,
			@RequestParam String sort, @RequestParam String order) {
		List<Object[]> data = authorRepository.getNumBooksByAuthor(page, size, sort, order);

		List<Map<String, String>> ret = new ArrayList<>(data.size());
		for (Object[] o : data) {
			Map<String, String> map = new HashMap<>();
			map.put("id", o[0].toString());
			map.put("sort", o[1].toString());
			map.put("total", o[2].toString());
			ret.add(map);
		}
		return ret;
	}

	@GetMapping(value = "/info/name", produces = MediaType.APPLICATION_JSON_VALUE)
	public MyAuthor getAuthorInfoByName(@RequestParam String author, @RequestParam String lang) {
		MyAuthor myAuthor = myAuthorRepository.findBySort(author);
		if (myAuthor == null) {

			myAuthor = wikipediaService.findAuthor(author, lang);

			if (myAuthor == null) {
				myAuthor = wikipediaService.findAuthor(author, "en");
			}

			if (myAuthor == null) {
				myAuthor = goodReadsService.findAuthor(author);
			}

			if (myAuthor != null) {
				myAuthor.setSort(author);
				Author auth = authorRepository.findBySort(author);
				myAuthor.setId(auth.getId());
				myAuthorRepository.save(myAuthor);
			}
		}
		return myAuthor;
	}

	@GetMapping(value = "/info/id", produces = MediaType.APPLICATION_JSON_VALUE)
	public Optional<MyAuthor> getAuthorInfoById(@RequestParam int id) {
		return myAuthorRepository.findById(id);
	}

	@GetMapping(value = "/nodata")
	public void getAllAuthorsNoData(@RequestParam String lang) {
		asyncService.getAllNoData(lang);
	}

	@GetMapping(value = "/data")
	public void getAllAuthorsData(@RequestParam String lang) {
		asyncService.getAllData(lang);
	}

	@GetMapping(value = "/stop")
	public void stop() {
		asyncService.stop();
	}

	@GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getAllAuthorsState() {
		return asyncService.getAllAuthorsState();
	}

	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, String>> getFavoriteBooks(@RequestParam int user) {
		List<Map<String, String>> list = new ArrayList<>();

		List<Integer> dat = myAuthorRepository.getFavoriteAuthors(user);

		for (Integer id : dat) {
			Object[] o = (Object[]) authorRepository.getNumBooksByAuthorId(id)[0];

			Map<String, String> map = new HashMap<>();
			map.put("id", o[0].toString());
			map.put("sort", o[1].toString());
			map.put("total", o[2].toString());
			list.add(map);

		}

		return list;
	}
	
	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public FavoriteAuthor getFavoriteAuthor(@RequestParam int author, @RequestParam int user) {
		FavoriteAuthor fb = favoriteAuthorRepository.getFavoriteAuthor(author, user);
		return fb;
	}

	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public void addFavoriteAuthors(@RequestParam int author, @RequestParam int user) {
		FavoriteAuthor fb = new FavoriteAuthor(user, author);
		favoriteAuthorRepository.save(fb);
	}

	@Transactional
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteFavoriteAuthors(@RequestParam int author, @RequestParam int user) {
		FavoriteAuthor fb = favoriteAuthorRepository.getFavoriteAuthor(author, user);
		favoriteAuthorRepository.delete(fb);
	}
}
