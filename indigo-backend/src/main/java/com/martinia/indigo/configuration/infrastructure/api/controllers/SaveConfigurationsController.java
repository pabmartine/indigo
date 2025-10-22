package com.martinia.indigo.configuration.infrastructure.api.controllers;

import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.usecases.SaveConfigurationsUseCase;
import com.martinia.indigo.configuration.infrastructure.api.mappers.ConfigurationDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/config")
@Tag(name = "Configuration", description = "API for managing application configurations")
public class SaveConfigurationsController {

	@Resource
	private SaveConfigurationsUseCase useCase;

	@Autowired
	protected ConfigurationDtoMapper mapper;

	@Operation(summary = "Save configurations",
			description = "Saves a list of configuration entries.",
			requestBody = @RequestBody(description = "List of configuration objects to be saved",
					required = true,
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = Configuration.class))),
			responses = {
					@ApiResponse(responseCode = "200", description = "Configurations successfully saved"),
					@ApiResponse(responseCode = "400", description = "Invalid configuration data supplied")
			})
	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> save(@org.springframework.web.bind.annotation.RequestBody final List<Configuration> configurations) {
		useCase.save(configurations);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
