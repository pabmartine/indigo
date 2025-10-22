package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByPathUseCase;
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
public class FindBookByPathController {

	@Resource
	private FindBookByPathUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Operation(summary = "Find book by path",
			description = "Retrieves a single book by its file path.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the book",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = BookDto.class))),
					@ApiResponse(responseCode = "404", description = "Book not found")
			})
	@GetMapping(value = "/path", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> getBookByPath(
			@Parameter(description = "The file path of the book to retrieve", example = "/books/my_book.epub") @RequestParam String path) {
		BookDto bookDto = useCase.findByPath(path).map(book -> mapper.domain2Dto(book)).orElse(null);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

}
