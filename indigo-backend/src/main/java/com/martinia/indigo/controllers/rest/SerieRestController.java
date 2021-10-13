package com.martinia.indigo.controllers.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.services.calibre.SerieService;
import com.martinia.indigo.utils.CoverComponent;

@RestController
@RequestMapping("/rest/serie")
public class SerieRestController {

	@Autowired
	private SerieService serieService;

	@Autowired
	private CoverComponent coverComponent;

	@GetMapping("/count")
	public ResponseEntity<Long> getTotal() {
		return new ResponseEntity<>(serieService.count(), HttpStatus.OK);

	}

	@GetMapping(value = "/numbooks", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, String>>> getNumBooksBySerie(@RequestParam int page, @RequestParam int size,
			@RequestParam String sort, @RequestParam String order) {

		List<Object[]> data = serieService.getNumBooksBySerie(page, size, sort, order);

		List<Map<String, String>> ret = new ArrayList<>(data.size());
		for (Object[] o : data) {
			Map<String, String> map = new HashMap<>();
			map.put("id", o[0].toString());
			map.put("name", o[1].toString());
			map.put("sort", o[2].toString());
			map.put("total", o[3].toString());
			ret.add(map);
		}

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> findPagesByBookId(@RequestParam int id) {

		String serie = serieService.getSerieByBook(id);

		Map<String, String> map = new HashMap<String, String>();
		map.put("serie", serie);

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getCover(@RequestParam int id) throws IOException {
		Map<String, String> map = null;

		String image = coverComponent.getCover(id);

		map = new HashMap<String, String>();
		map.put("image", image);

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}