package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/serie")
@Tag(name = "Series", description = "API for managing book series")
public class FindCoverSerieController {

	@Resource
	private FindCoverSerieUseCase useCase;

	@Operation(summary = "Get series cover image",
			description = "Retrieves the base64 encoded cover image for a specific book series.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the series cover image",
							content = @Content(mediaType = "application/json",
									schema = @Schema(type = "object", example = "{\"image\": \"base64encodedstring...\"}"))),
					@ApiResponse(responseCode = "404", description = "Series cover not found")
			})
	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getCover(
			@Parameter(description = "The name of the series to retrieve the cover for", example = "The Dark Tower") @RequestParam String serie) {

		final String image = useCase.getCover(serie);

		Map<String, String> map = new HashMap<>();
		map.put("image", image);

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
