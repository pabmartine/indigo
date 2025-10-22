package com.martinia.indigo.notification.infrastructure.api.controllers;

import com.martinia.indigo.notification.domain.ports.usecases.DeleteNotificationUseCase;
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
@RequestMapping("/api/notification")
@Tag(name = "Notifications", description = "API for managing user notifications")
public class DeleteNotificationController {

	@Resource
	private DeleteNotificationUseCase useCase;

	@Operation(summary = "Delete a notification",
			description = "Deletes a specific notification by its ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Notification successfully deleted"),
					@ApiResponse(responseCode = "400", description = "Invalid notification ID supplied")
			})
	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(
			@Parameter(description = "The ID of the notification to delete", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam String id) {
		useCase.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
