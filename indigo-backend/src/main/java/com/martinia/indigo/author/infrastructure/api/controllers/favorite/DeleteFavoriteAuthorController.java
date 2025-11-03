package com.martinia.indigo.author.infrastructure.api.controllers.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.DeleteFavoriteAuthorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/author")
@Tag(name = "Authors", description = "API for author management")
public class DeleteFavoriteAuthorController {

	@Resource
	private DeleteFavoriteAuthorUseCase useCase;

	@Transactional
	@Operation(summary = "Delete a favorite author",
			description = "Removes an author from a user's list of favorite authors.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Author successfully removed from favorites"),
					@ApiResponse(responseCode = "400", description = "Invalid request parameters")
			})
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deleteFavoriteAuthors(
			@Parameter(description = "The name of the author to remove from favorites", example = "Stephen King") @RequestParam String author,
			@Parameter(description = "The username of the user removing the favorite author", example = "john.doe") @RequestParam String user) {
		useCase.deleteFavoriteAuthor(user, author);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
