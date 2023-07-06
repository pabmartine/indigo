package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.book.domain.service.favorite.AddFavoriteBookUseCase;
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
public class AddFavoriteBookController {

	@Resource
	private AddFavoriteBookUseCase useCase;

	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> addFavoriteBook(@RequestParam String book, @RequestParam String user) {
		useCase.addFavoriteBook(user, book);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
