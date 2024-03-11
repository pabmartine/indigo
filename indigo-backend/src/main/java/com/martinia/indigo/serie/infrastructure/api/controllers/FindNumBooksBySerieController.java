package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.serie.domain.ports.usecases.FindNumBooksBySerieUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/serie")
public class FindNumBooksBySerieController {

	@Resource
	private FindNumBooksBySerieUseCase useCase;

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, String>>> getNumBooksBySerie(@RequestParam List<String> languages, @RequestParam int page,
			@RequestParam int size, @RequestParam String sort, @RequestParam String order) {

		Map<String, Long> data = useCase.getNumBooksBySerie(languages, page, size, sort, order);

		List<Map<String, String>> ret = new ArrayList<>();
		for (String key : data.keySet()) {
			Map<String, String> map = new HashMap<>();
			map.put("name", key);
			map.put("numBooks", data.get(key).toString());
			ret.add(map);
		}

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

}
