package com.martinia.indigo.user.infrastructure.api.controllers;

import com.martinia.indigo.user.infrastructure.api.model.UserDto;
import com.martinia.indigo.user.infrastructure.api.mappers.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "API for managing user accounts")
public class FindUserByUsernameController {

	@Resource
	private FindUserByUsernameUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@Operation(summary = "Find user by username",
			description = "Retrieves a single user by their username.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the user",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = UserDto.class))),
					@ApiResponse(responseCode = "404", description = "User not found")
			})
	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> get(
			@Parameter(description = "The username of the user to retrieve", example = "john.doe") @RequestParam final String username) {
		final Optional<User> user = useCase.findByUsername(username);
		final UserDto userDto = user.map(usr -> mapper.domain2Dto(usr)).orElse(null);
		return new ResponseEntity<>(userDto, HttpStatus.OK);
	}

}
