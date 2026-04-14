package com.martinia.indigo.user.infrastructure.api.controllers;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
import com.martinia.indigo.user.infrastructure.api.mappers.UserDtoMapper;
import com.martinia.indigo.user.infrastructure.api.model.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "API for managing user accounts")
public class FindCurrentUserController {

	@Resource
	private FindUserByUsernameUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@Operation(summary = "Get current authenticated user",
			description = "Retrieves the currently authenticated user from the security context.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the current user",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = UserDto.class))),
					@ApiResponse(responseCode = "401", description = "User not authenticated")
			})
	@GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		Optional<User> user = useCase.findByUsername(authentication.getName());
		return user.map(value -> new ResponseEntity<>(mapper.domain2Dto(value), HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
