package com.martinia.indigo.author.infrastructure.api.controllers.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.AddFavoriteAuthorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/author")
@Tag(name = "Authors", description = "API for author management")
public class AddFavoriteAuthorController {

	@Resource
	private AddFavoriteAuthorUseCase useCase;

	@Operation(summary = "Add a favorite author",
			description = "Adds an author to a user's list of favorite authors.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Author successfully added to favorites"),
					@ApiResponse(responseCode = "400", description = "Invalid request parameters")
			})
	@PostMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> addFavoriteAuthors(
			@Parameter(description = "The name of the author to add to favorites", example = "Stephen King") @RequestParam String author,
			@Parameter(description = "The username of the user adding the favorite author", example = "john.doe") @RequestParam String user) {
		useCase.addFavoriteAuthor(user, author);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
