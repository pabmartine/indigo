package com.martinia.indigo.notification.infrastructure.api.controllers;

import com.martinia.indigo.notification.infrastructure.api.model.NotificationDto;
import com.martinia.indigo.notification.infrastructure.api.mappers.NotificationDtoMapper;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.usecases.FindNotReadNotificationsByUserUseCase;
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
import java.util.List;

@RestController
@RequestMapping("/api/notification")
@Tag(name = "Notifications", description = "API for managing user notifications")
public class FindNotReadNotificationsByUserController {

	@Resource
	private FindNotReadNotificationsByUserUseCase useCase;

	@Resource
	private NotificationDtoMapper mapper;

	@Operation(summary = "Find unread notifications by user",
			description = "Retrieves all unread notifications for a specific user.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved unread notifications",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = NotificationDto.class)))
			})
	@GetMapping(value = "/not_read_user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAllNotReadByUser(
			@Parameter(description = "The username to retrieve unread notifications for", example = "john.doe") @RequestParam String user) {
		List<Notification> notifications = useCase.findNotReadUser(user);
		List<NotificationDto> notificationsDto = mapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

}
