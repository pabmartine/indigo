package com.martinia.indigo.user.infrastructure.api.controllers;

import com.martinia.indigo.user.infrastructure.api.model.UserDto;
import com.martinia.indigo.user.infrastructure.api.mappers.UserDtoMapper;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByIdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "API for managing user accounts")
public class FindUserByIdController {

	@Resource
	private FindUserByIdUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@Operation(summary = "Find user by ID",
			description = "Retrieves a single user by their unique identifier.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the user",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = UserDto.class))),
					@ApiResponse(responseCode = "404", description = "User not found")
			})
	@GetMapping(value = "/getById", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> getById(
			@Parameter(description = "The ID of the user to retrieve", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam final String id) {
		final UserDto userDto = useCase.findById(id).map(user -> mapper.domain2Dto(user)).orElse(null);
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(userDto, headers, HttpStatus.OK);
	}

}
