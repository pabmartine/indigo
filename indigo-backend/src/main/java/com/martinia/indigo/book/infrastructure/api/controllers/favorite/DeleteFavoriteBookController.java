package com.martinia.indigo.book.infrastructure.api.controllers.favorite;

import com.martinia.indigo.book.domain.ports.usecases.favorite.DeleteFavoriteBookUseCase;
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

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class DeleteFavoriteBookController {

	@Resource
	private DeleteFavoriteBookUseCase useCase;

	@Operation(summary = "Delete a favorite book",
			description = "Removes a specified book from a user's list of favorite books.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Book successfully removed from favorites"),
					@ApiResponse(responseCode = "400", description = "Invalid request parameters")
			})
	@DeleteMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> deleteFavoriteBooks(
			@Parameter(description = "The ID of the book to remove from favorites", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam String book,
			@Parameter(description = "The username of the user removing the favorite book", example = "john.doe") @RequestParam String user) {
		useCase.deleteFavoriteBook(user, book);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
