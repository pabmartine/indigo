package com.martinia.indigo.adapters.in.rest.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.util.UtilComponent;
import com.martinia.indigo.ports.in.rest.BookService;

@RestController
@RequestMapping("/rest/serie")
public class SerieRestController {

	@Autowired
	private BookService bookService;

	@Autowired
	private UtilComponent coverComponent;

	@GetMapping("/count")
	public ResponseEntity<Long> getNumSeries(@RequestParam List<String> languages) {
		return new ResponseEntity<>(bookService.getNumSeries(languages), HttpStatus.OK);
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, String>>> getNumBooksBySerie(@RequestParam List<String> languages,
			@RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String order) {

		Map<String, Long> data = bookService.getNumBooksBySerie(languages, page, size, sort, order);

		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		for (String key : data.keySet()) {
			Map<String, String> map = new HashMap<>();
			map.put("name", key);
			map.put("numBooks", data.get(key)
					.toString());
			ret.add(map);
		}

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getCover(@RequestParam String serie) throws IOException {
		Map<String, String> map = null;

		List<Book> books = bookService.findBooksBySerie(serie.replace("@_@", "&"));
		List<Book> sorted = books.stream()
				.sorted(Comparator.comparingInt(b -> b.getSerie()
						.getIndex()))
				.collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(sorted)) {
			String image = sorted.get(0)
					.getImage();

			map = new HashMap<String, String>();
			map.put("image", image);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
