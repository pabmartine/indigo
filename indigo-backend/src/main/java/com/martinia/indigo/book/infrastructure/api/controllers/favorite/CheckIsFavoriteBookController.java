package com.martinia.indigo.book.infrastructure.api.controllers.favorite;

import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.ports.usecases.favorite.CheckIsFavoriteBookUseCase;
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
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class CheckIsFavoriteBookController {

	@Resource
	private CheckIsFavoriteBookUseCase useCase;

	@Resource
	protected BookDtoMapper mapper;

	@Operation(summary = "Check if a book is favorite",
			description = "Checks if a specific book is marked as favorite by a given user.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully checked if book is favorite",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
			})
	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> isFavoriteBook(
			@Parameter(description = "The ID of the book to check", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam String book,
			@Parameter(description = "The username of the user", example = "john.doe") @RequestParam String user) {
		Boolean isFavorite = useCase.isFavoriteBook(user, book);
		return new ResponseEntity<>(isFavorite, HttpStatus.OK);
	}

}
