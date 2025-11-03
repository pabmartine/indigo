package com.martinia.indigo.file.infrastructure.api.controllers;

import com.martinia.indigo.file.domain.ports.usecases.CountEpubFilesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/file")
@Tag(name = "Files", description = "API for file management")
public class CountEpubFilesController {

	@Resource
	private CountEpubFilesUseCase useCase;

	@Operation(summary = "Count EPUB files",
			description = "Retrieves the total number of EPUB files in the system.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the count of EPUB files",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
			})
	@GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Long> count() {
		return new ResponseEntity<>(useCase.count(), HttpStatus.OK);
	}

}
