package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.domain.ports.usecases.FindStatusMetadataUseCase;
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
import java.util.Map;

@RestController
@RequestMapping("/api/metadata")
@Tag(name = "Metadata", description = "API for managing book metadata")
public class FindStatusMetadataController {

	@Resource
	private FindStatusMetadataUseCase useCase;

	@Operation(summary = "Get metadata processing status",
			description = "Retrieves the current status of the metadata processing service.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved metadata status",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = Map.class)))
			})
	@GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getStatus() {
		return new ResponseEntity<>(useCase.getStatus(), HttpStatus.OK);
	}

}
