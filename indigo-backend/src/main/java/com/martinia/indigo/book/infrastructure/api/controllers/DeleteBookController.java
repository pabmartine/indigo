package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.book.domain.ports.usecases.DeleteBookUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/book")
public class DeleteBookController {
	@Resource
	private DeleteBookUseCase useCase;

	@DeleteMapping(value = "/delete")
	public ResponseEntity delete(@RequestParam String id) {
		useCase.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
