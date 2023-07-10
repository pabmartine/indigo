package com.martinia.indigo.book.infrastructure.api;

import com.martinia.indigo.book.domain.ports.usecases.CountAllBooksUseCase;
import com.martinia.indigo.common.model.Search;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/book")
public class CountAllBooksController {

	@Resource
	private CountAllBooksUseCase useCase;

	@PostMapping(value = "/count/search/advance", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Long> getTotalAdvSearch(@RequestBody(required = false) Search search) {
		return new ResponseEntity<>(useCase.count(search), HttpStatus.OK);
	}

}
