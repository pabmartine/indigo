package com.martinia.indigo.author.infrastructure.api.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.AddFavoriteAuthorUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/author")
public class AddFavoriteAuthorController {

	@Resource
	private AddFavoriteAuthorUseCase useCase;

	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> addFavoriteAuthors(@RequestParam String author, @RequestParam String user) {
		useCase.addFavoriteAuthor(user, author);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
