package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/serie")
public class FindCoverSerieController {

	@Resource
	private FindCoverSerieUseCase useCase;

	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getCover(@RequestParam String serie) {

		final String image = useCase.getCover(serie);

		Map<String, String> map = new HashMap<>();
		map.put("image", image);

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
