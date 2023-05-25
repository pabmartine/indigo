package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.dtos.NotificationDto;
import com.martinia.indigo.adapters.in.rest.mappers.NotificationDtoMapper;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.service.FindNotReadNotificationsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/rest/notification")
public class FindNotReadNotificationsController {

	@Resource
	private FindNotReadNotificationsUseCase useCase;

	@Resource
	protected NotificationDtoMapper mapper;

	@GetMapping(value = "/not_read", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAllNotRead() {
		List<Notification> notifications = useCase.findNotReadAdmin();
		List<NotificationDto> notificationsDto = mapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

}
