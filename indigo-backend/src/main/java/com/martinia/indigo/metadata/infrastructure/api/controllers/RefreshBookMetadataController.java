package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshBookMetadataUseCase;
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
@RequestMapping("/api/metadata")
@Tag(name = "Metadata", description = "API for managing book metadata")
public class RefreshBookMetadataController {

	@Resource
	private RefreshBookMetadataUseCase useCase;

	@Resource
	protected BookDtoMapper mapper;

	@Operation(summary = "Refresh book metadata",
			description = "Refreshes the metadata for a specific book based on the provided language.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully refreshed book metadata",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = BookDto.class))),
					@ApiResponse(responseCode = "404", description = "Book not found")
			})
	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> refreshBook(
			@Parameter(description = "The ID of the book to refresh metadata for", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam String book,
			@Parameter(description = "The language for which to refresh book metadata", example = "en") @RequestParam String lang) {
		BookDto bookDto = useCase.findBookMetadata(book, lang).map(_book -> mapper.domain2Dto(_book)).orElse(null);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

}
