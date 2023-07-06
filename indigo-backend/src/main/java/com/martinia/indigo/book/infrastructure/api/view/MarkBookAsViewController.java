package com.martinia.indigo.book.infrastructure.api.view;

import com.martinia.indigo.book.domain.service.view.MarkBookAsViewUseCase;
import com.martinia.indigo.domain.model.View;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/book")
public class MarkBookAsViewController {

	@Resource
	private MarkBookAsViewUseCase useCase;

	@PostMapping(value = "/view", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> view(@RequestParam String book, @RequestParam String user) {
		useCase.save(new View(book, user));
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
