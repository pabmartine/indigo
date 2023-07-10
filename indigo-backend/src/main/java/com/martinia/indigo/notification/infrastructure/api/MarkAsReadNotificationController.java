package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.notification.domain.ports.usecases.MarkAsReadNotificationUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/notification")
public class MarkAsReadNotificationController {

	@Resource
	private MarkAsReadNotificationUseCase useCase;

	@GetMapping(value = "/read", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> markAsRead(@RequestParam String id, @RequestParam String user) {
		useCase.markAsRead(id, user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
