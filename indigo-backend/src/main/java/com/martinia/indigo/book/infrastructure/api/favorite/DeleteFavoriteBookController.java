package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.book.domain.ports.usecases.favorite.DeleteFavoriteBookUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/book")
public class DeleteFavoriteBookController {

	@Resource
	private DeleteFavoriteBookUseCase useCase;

	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deleteFavoriteBooks(@RequestParam String book, @RequestParam String user) {
		useCase.deleteFavoriteBook(user, book);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
