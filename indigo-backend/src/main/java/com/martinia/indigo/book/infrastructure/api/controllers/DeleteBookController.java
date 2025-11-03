package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.book.domain.ports.usecases.DeleteBookUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class DeleteBookController {
	@Resource
	private DeleteBookUseCase useCase;

	@Operation(summary = "Delete a book by ID",
			description = "Deletes a book from the system using its unique identifier.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Book successfully deleted"),
					@ApiResponse(responseCode = "400", description = "Invalid book ID supplied")
			})
	@DeleteMapping(value = "/delete")
	public ResponseEntity delete(
			@Parameter(description = "The unique identifier of the book to delete", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam String id) {
		useCase.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
