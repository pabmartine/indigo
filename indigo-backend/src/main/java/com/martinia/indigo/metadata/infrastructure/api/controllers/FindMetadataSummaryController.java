package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.domain.ports.usecases.FindMetadataSummaryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/metadata")
@Tag(name = "Metadata", description = "API for managing book metadata")
public class FindMetadataSummaryController {

	@Resource
	private FindMetadataSummaryUseCase useCase;

	@Operation(summary = "Get metadata summary counters",
			description = "Retrieves summary counters for books, authors and reviews.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved metadata summary",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = Map.class)))
			})
	@GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Long>> getSummary() {
		return new ResponseEntity<>(useCase.getSummary(), HttpStatus.OK);
	}
}
