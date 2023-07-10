package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.notification.domain.ports.usecases.DeleteNotificationUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/notification")
public class DeleteNotificationController {

	@Resource
	private DeleteNotificationUseCase useCase;

	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(@RequestParam String id) {
		useCase.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
