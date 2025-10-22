package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.serie.domain.ports.usecases.FindNumSeriesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/serie")
@Tag(name = "Series", description = "API for managing book series")
public class FindNumSeriesController {

	@Resource
	private FindNumSeriesUseCase useCase;

	@Operation(summary = "Get number of series",
			description = "Retrieves the total number of book series, optionally filtered by language.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the count of series",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
			})
	@GetMapping("/count")
	public ResponseEntity<Long> getNumSeries(
			@Parameter(description = "List of languages to filter series by (e.g., 'en', 'es')", example = "[\"en\"]") @RequestParam List<String> languages) {
		return new ResponseEntity<>(useCase.getNumSeries(languages), HttpStatus.OK);
	}

}
