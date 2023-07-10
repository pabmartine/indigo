package com.martinia.indigo.author.infrastructure.api;

import com.martinia.indigo.author.domain.ports.usecases.CountAllAuthorsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/rest/author")
public class CountAllAuthorsController {

	@Resource
	private CountAllAuthorsUseCase useCase;

	@GetMapping("/count")
	public ResponseEntity<Long> count(@RequestParam List<String> languages) {
		return new ResponseEntity<>(useCase.count(languages), HttpStatus.OK);
	}

}
