package com.martinia.indigo.notification.infrastructure.api.controllers;

import com.martinia.indigo.notification.domain.ports.usecases.MarkAsReadNotificationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/notification")
@Tag(name = "Notifications", description = "API for managing user notifications")
public class MarkAsReadNotificationController {

	@Resource
	private MarkAsReadNotificationUseCase useCase;

	@Operation(summary = "Mark notification as read",
			description = "Marks a specific notification as read for a given user.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Notification successfully marked as read"),
					@ApiResponse(responseCode = "400", description = "Invalid notification ID or user supplied")
			})
	@GetMapping(value = "/read", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> markAsRead(
			@Parameter(description = "The ID of the notification to mark as read", example = "60c72b2f9b1e8c001c8e4a0a") @RequestParam String id,
			@Parameter(description = "Deprecated: the authenticated user is used instead") @RequestParam(required = false) String user,
			Authentication authentication) {
		useCase.markAsRead(id, authentication != null ? authentication.getName() : user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
