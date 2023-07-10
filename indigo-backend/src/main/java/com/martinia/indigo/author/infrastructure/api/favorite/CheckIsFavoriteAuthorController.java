package com.martinia.indigo.author.infrastructure.api.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.CheckIsFavoriteAuthorUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/author")
public class CheckIsFavoriteAuthorController {

	@Resource
	private CheckIsFavoriteAuthorUseCase useCase;

	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> getFavoriteAuthor(@RequestParam String author, @RequestParam String user) {
		Boolean isFavorite = useCase.isFavoriteAuthor(user, author);
		return new ResponseEntity<>(isFavorite, HttpStatus.OK);
	}

}
