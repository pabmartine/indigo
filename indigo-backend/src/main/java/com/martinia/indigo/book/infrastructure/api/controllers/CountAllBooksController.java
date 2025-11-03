package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.book.domain.ports.usecases.CountAllBooksUseCase;
import com.martinia.indigo.common.domain.model.Search;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class CountAllBooksController {

	@Resource
	private CountAllBooksUseCase useCase;

	@Operation(summary = "Count all books with advanced search",
			description = "Retrieves the total number of books matching the criteria provided in the advanced search object.",
			requestBody = @RequestBody(description = "Search object with criteria for filtering books (optional)",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = Search.class))),
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the count of books",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
			})
	@PostMapping(value = "/count/search/advance", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Long> getTotalAdvSearch(@org.springframework.web.bind.annotation.RequestBody(required = false) Search search) {
		return new ResponseEntity<>(useCase.count(search), HttpStatus.OK);
	}

}
