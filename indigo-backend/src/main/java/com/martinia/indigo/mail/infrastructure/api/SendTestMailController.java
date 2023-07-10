package com.martinia.indigo.mail.infrastructure.api;

import com.martinia.indigo.mail.domain.ports.usecases.SendTestMailUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/mail")
public class SendTestMailController {

	@Resource
	private SendTestMailUseCase useCase;

	@GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> test(@RequestParam final String address) {
		useCase.test(address);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
