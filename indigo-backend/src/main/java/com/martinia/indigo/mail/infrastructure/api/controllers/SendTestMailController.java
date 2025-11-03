package com.martinia.indigo.mail.infrastructure.api.controllers;

import com.martinia.indigo.mail.domain.ports.usecases.SendTestMailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/mail")
@Tag(name = "Mail", description = "API for sending emails")
public class SendTestMailController {

	@Resource
	private SendTestMailUseCase useCase;

	@Operation(summary = "Send test mail",
			description = "Sends a test email to a specified address to verify mail server configuration.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Test email sent successfully"),
					@ApiResponse(responseCode = "400", description = "Invalid email address or test email sending failed")
			})
	@GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> test(
			@Parameter(description = "The recipient's email address for the test mail", example = "test@example.com") @RequestParam final String address) {
		useCase.test(address);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
