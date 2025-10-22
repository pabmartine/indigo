package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.book.domain.ports.usecases.EditBookUseCase;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class EditBookController {
	@Resource
	private EditBookUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Operation(summary = "Edit an existing book",
			description = "Updates the details of an existing book using the provided BookDto.",
			requestBody = @RequestBody(description = "Book object to be updated",
					required = true,
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
			responses = {
					@ApiResponse(responseCode = "200", description = "Book successfully updated"),
					@ApiResponse(responseCode = "400", description = "Invalid book data supplied")
			})
	@PutMapping(value = "/edit")
	public ResponseEntity edit(@Valid @org.springframework.web.bind.annotation.RequestBody BookDto bookDto) {
		useCase.edit(mapper.dto2domain(bookDto));
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
