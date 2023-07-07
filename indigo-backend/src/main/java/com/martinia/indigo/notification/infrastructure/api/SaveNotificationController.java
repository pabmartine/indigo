package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.notification.infrastructure.model.NotificationDto;
import com.martinia.indigo.notification.infrastructure.mapper.NotificationDtoMapper;
import com.martinia.indigo.notification.domain.service.SaveNotificationUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/notification")
public class SaveNotificationController {

	@Resource
	private SaveNotificationUseCase useCase;

	@Resource
	private NotificationDtoMapper mapper;

	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> save(@RequestBody NotificationDto notificationDto) {
		useCase.save(mapper.dto2Domain(notificationDto));
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
