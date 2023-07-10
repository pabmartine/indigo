package com.martinia.indigo.author.infrastructure.api.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.DeleteFavoriteAuthorUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@RestController
@RequestMapping("/api/author")
public class DeleteFavoriteAuthorController {

	@Resource
	private DeleteFavoriteAuthorUseCase useCase;

	@Transactional
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deleteFavoriteAuthors(@RequestParam String author, @RequestParam String user) {
		useCase.deleteFavoriteAuthor(user, author);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
