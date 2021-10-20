package com.martinia.indigo.adapters.in.rest.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.adapters.in.rest.dtos.NotificationDto;
import com.martinia.indigo.adapters.in.rest.mappers.NotificationDtoMapper;
import com.martinia.indigo.domain.model.Notification;
import com.martinia.indigo.ports.in.rest.NotificationService;

@RestController
@RequestMapping("/rest/notification")
public class NotificationRestController {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	protected NotificationDtoMapper notificationDtoMapper;

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAll() {
		List<Notification> notifications = notificationService.findAllByOrderBySendDateDesc();
		List<NotificationDto> notificationsDto = notificationDtoMapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

	@GetMapping(value = "/not_read", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAllNotRead() {
		List<Notification> notifications = notificationService.findNotReadAdmin();
		List<NotificationDto> notificationsDto = notificationDtoMapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

	@GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAllByUser(@RequestParam String user) {
		List<Notification> notifications = notificationService.findByUser(user);
		List<NotificationDto> notificationsDto = notificationDtoMapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

	@GetMapping(value = "/not_read_user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> findAllNotReadByUser(@RequestParam String user) {
		List<Notification> notifications = notificationService.findNotReadUser(user);
		List<NotificationDto> notificationsDto = notificationDtoMapper.domains2Dtos(notifications);
		return new ResponseEntity<>(notificationsDto, HttpStatus.OK);
	}

	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> save(@RequestBody NotificationDto notificationDto) {
		notificationService.save(notificationDtoMapper.dto2Domain(notificationDto));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/read", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> markAsRead(@RequestParam String id, @RequestParam String user) {
		notificationService.markAsRead(id, user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(@RequestParam String id) {
		notificationService.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
