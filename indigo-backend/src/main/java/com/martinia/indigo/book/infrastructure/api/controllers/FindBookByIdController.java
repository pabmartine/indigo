package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByIdUseCase;
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
public class FindBookByIdController {

	@Resource
	private FindBookByIdUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Operation(summary = "Find book by ID",
			description = "Retrieves a single book by its unique identifier.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the book",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = BookDto.class))),
					@ApiResponse(responseCode = "404", description = "Book not found")
			})
	@GetMapping(value = "/id", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> getBookById(
			@Parameter(description = "The unique identifier of the book to retrieve", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam String id) {
		BookDto bookDto = useCase.findById(id).map(book -> mapper.domain2Dto(book)).orElse(null);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

}
