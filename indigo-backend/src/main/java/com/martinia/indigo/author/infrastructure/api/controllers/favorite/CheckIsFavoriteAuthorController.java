package com.martinia.indigo.author.infrastructure.api.controllers.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.CheckIsFavoriteAuthorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/author")
@Tag(name = "Authors", description = "API for author management")
public class CheckIsFavoriteAuthorController {

	@Resource
	private CheckIsFavoriteAuthorUseCase useCase;

	@Operation(summary = "Check if author is favorite",
			description = "Checks if a specific author is marked as favorite by a given user.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully checked if author is favorite",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
			})
	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> getFavoriteAuthor(
			@Parameter(description = "The name of the author to check", example = "Stephen King") @RequestParam String author,
			@Parameter(description = "The username of the user", example = "john.doe") @RequestParam String user) {
		Boolean isFavorite = useCase.isFavoriteAuthor(user, author);
		return new ResponseEntity<>(isFavorite, HttpStatus.OK);
	}

}
