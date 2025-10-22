package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.author.infrastructure.api.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.api.mappers.AuthorDtoMapper;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshAuthorMetadataUseCase;
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
public class RefreshAuthorMetadataController {

	@Resource
	private RefreshAuthorMetadataUseCase useCase;

	@Resource
	protected AuthorDtoMapper mapper;

	@Operation(summary = "Refresh author metadata",
			description = "Refreshes the metadata for a specific author based on the provided language.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully refreshed author metadata",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = AuthorDto.class))),
					@ApiResponse(responseCode = "404", description = "Author not found")
			})
	@GetMapping(value = "/author", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthorDto> refreshAuthor(
			@Parameter(description = "The language for which to refresh author metadata", example = "en") @RequestParam String lang,
			@Parameter(description = "The name of the author to refresh metadata for", example = "Stephen King") @RequestParam String author) {
		AuthorDto authorDto = useCase.findAuthorMetadata(author, lang).map(_author -> mapper.domain2Dto(_author)).orElse(null);
		return new ResponseEntity<>(authorDto, HttpStatus.OK);
	}

}
