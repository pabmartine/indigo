package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.serie.domain.ports.usecases.FindNumSeriesUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/serie")
public class FindNumSeriesController {

	@Resource
	private FindNumSeriesUseCase useCase;

	@GetMapping("/count")
	public ResponseEntity<Long> getNumSeries(@RequestParam List<String> languages) {
		return new ResponseEntity<>(useCase.getNumSeries(languages), HttpStatus.OK);
	}

}
