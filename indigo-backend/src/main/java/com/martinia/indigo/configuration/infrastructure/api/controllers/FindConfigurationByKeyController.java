package com.martinia.indigo.configuration.infrastructure.api.controllers;

import com.martinia.indigo.configuration.domain.ports.usecases.FindConfigurationByKeyUseCase;
import com.martinia.indigo.configuration.infrastructure.api.mappers.ConfigurationDtoMapper;
import com.martinia.indigo.configuration.infrastructure.api.model.ConfigurationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/config")
@Tag(name = "Configuration", description = "API for managing application configurations")
public class FindConfigurationByKeyController {

	@Resource
	private FindConfigurationByKeyUseCase useCase;

	@Autowired
	protected ConfigurationDtoMapper mapper;

	@Operation(summary = "Get configuration by key",
			description = "Retrieves a configuration entry by its unique key.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the configuration",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = ConfigurationDto.class))),
					@ApiResponse(responseCode = "404", description = "Configuration not found for the given key")
			})
	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConfigurationDto> get(
			@Parameter(description = "The key of the configuration to retrieve", example = "app.version") @RequestParam final String key) {
		ConfigurationDto configurationDto = useCase.findByKey(key).map(conf -> mapper.domain2Dto(conf)).orElse(null);
		return new ResponseEntity<>(configurationDto, HttpStatus.OK);
	}


}
