package com.martinia.indigo.user.infrastructure.api.controllers;

import com.martinia.indigo.user.infrastructure.api.mappers.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.UpdateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "API for managing user accounts")
public class UpdateUserController {

	@Resource
	private UpdateUserUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@Operation(summary = "Update user",
			description = "Updates an existing user account.",
			requestBody = @RequestBody(description = "User object with updated details",
					required = true,
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
			responses = {
					@ApiResponse(responseCode = "200", description = "User successfully updated"),
					@ApiResponse(responseCode = "400", description = "Invalid user data supplied")
			})
	@PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> update(@org.springframework.web.bind.annotation.RequestBody final User user) {
		useCase.update(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
