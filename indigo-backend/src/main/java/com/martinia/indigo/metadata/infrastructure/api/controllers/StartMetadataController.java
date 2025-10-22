package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/metadata")
@Tag(name = "Metadata", description = "API for managing book metadata")
public class StartMetadataController {

	@Resource
	private StartMetadataUseCase useCase;

	@Operation(summary = "Start metadata processing",
			description = "Initiates the metadata processing for a specified language, type, and entity.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Metadata processing started successfully"),
					@ApiResponse(responseCode = "400", description = "Invalid request parameters")
			})
	@GetMapping(value = "/start")
	public ResponseEntity<Void> initialLoad(
			@Parameter(description = "The language for metadata processing", example = "en") @RequestParam String lang,
			@Parameter(description = "The type of metadata to process (e.g., 'author', 'book')", example = "book") @RequestParam String type,
			@Parameter(description = "The entity to process metadata for (e.g., 'all', 'specific_id')", example = "all") @RequestParam String entity) {
		useCase.start(lang, type, entity);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
