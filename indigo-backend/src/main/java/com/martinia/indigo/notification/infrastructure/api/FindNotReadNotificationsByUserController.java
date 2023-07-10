package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.notification.infrastructure.model.NotificationDto;
import com.martinia.indigo.notification.infrastructure.mapper.NotificationDtoMapper;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.usecases.FindNotReadNotificationsByUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/rest/notification")
public class FindNotReadNotificationsByUserController {

	@Resource
	private FindNotReadNotificationsByUserUseCase useCase;

	@Resource
	private NotificationDtoMapper mapper;

	@GetMapping(value = "/not_read_user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAllNotReadByUser(@RequestParam String user) {
		List<Notification> notifications = useCase.findNotReadUser(user);
		List<NotificationDto> notificationsDto = mapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

}
