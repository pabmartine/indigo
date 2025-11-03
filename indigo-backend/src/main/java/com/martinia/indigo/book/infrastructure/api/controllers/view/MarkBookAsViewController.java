package com.martinia.indigo.book.infrastructure.api.controllers.view;

import com.martinia.indigo.book.domain.ports.usecases.view.MarkBookAsViewUseCase;
import com.martinia.indigo.common.domain.model.View;
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
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class MarkBookAsViewController {

	@Resource
	private MarkBookAsViewUseCase useCase;

	@Operation(summary = "Mark book as viewed",
			description = "Marks a specific book as viewed by a user.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Book successfully marked as viewed"),
					@ApiResponse(responseCode = "400", description = "Invalid request parameters")
			})
	@PostMapping(value = "/view", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> view(
			@Parameter(description = "The ID of the book to mark as viewed", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam String book,
			@Parameter(description = "The username of the user who viewed the book", example = "john.doe") @RequestParam String user) {
		useCase.save(new View(book, user));
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
