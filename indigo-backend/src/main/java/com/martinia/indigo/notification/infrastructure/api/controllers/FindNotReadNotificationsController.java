package com.martinia.indigo.notification.infrastructure.api.controllers;

import com.martinia.indigo.notification.infrastructure.api.model.NotificationDto;
import com.martinia.indigo.notification.infrastructure.api.mappers.NotificationDtoMapper;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.usecases.FindNotReadNotificationsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class FindNotReadNotificationsController {

	@Resource
	private FindNotReadNotificationsUseCase useCase;

	@Resource
	private NotificationDtoMapper mapper;

	@GetMapping(value = "/not_read", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAllNotRead() {
		List<Notification> notifications = useCase.findNotReadAdmin();
		List<NotificationDto> notificationsDto = mapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

}
