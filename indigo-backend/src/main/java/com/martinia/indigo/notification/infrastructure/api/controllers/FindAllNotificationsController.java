package com.martinia.indigo.notification.infrastructure.api.controllers;

import com.martinia.indigo.notification.infrastructure.api.model.NotificationDto;
import com.martinia.indigo.notification.infrastructure.api.mappers.NotificationDtoMapper;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.usecases.FindAllNotificationsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/notification")
@Tag(name = "Notifications", description = "API for managing user notifications")
public class FindAllNotificationsController {

	@Resource
	private FindAllNotificationsUseCase useCase;

	@Resource
	private NotificationDtoMapper mapper;

	@Operation(summary = "Find all notifications",
			description = "Retrieves all notifications in the system, ordered by send date in descending order.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved all notifications",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = NotificationDto.class)))
			})
	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAll() {
		List<Notification> notifications = useCase.findAllByOrderBySendDateDesc();
		List<NotificationDto> notificationsDto = mapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

}
