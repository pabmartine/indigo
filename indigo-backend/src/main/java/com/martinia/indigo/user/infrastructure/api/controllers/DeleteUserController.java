package com.martinia.indigo.user.infrastructure.api.controllers;

import com.martinia.indigo.user.infrastructure.api.mappers.UserDtoMapper;
import com.martinia.indigo.user.domain.ports.usecases.DeleteUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "API for managing user accounts")
public class DeleteUserController {

	@Resource
	private DeleteUserUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@Operation(summary = "Delete user",
			description = "Deletes a user account by their ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "User successfully deleted"),
					@ApiResponse(responseCode = "400", description = "Invalid user ID supplied")
			})
	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(
			@Parameter(description = "The ID of the user to delete", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam final String id) {
		useCase.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
