package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.domain.ports.usecases.StopMetadataUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/metadata")
@Tag(name = "Metadata", description = "API for managing book metadata")
public class StopMetadataController {

	@Resource
	private StopMetadataUseCase useCase;

	@Operation(summary = "Stop metadata processing",
			description = "Stops the currently running metadata processing service.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Metadata processing stopped successfully")
			})
	@GetMapping(value = "/stop")
	public ResponseEntity<Void> stop() {
		useCase.stop();
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
