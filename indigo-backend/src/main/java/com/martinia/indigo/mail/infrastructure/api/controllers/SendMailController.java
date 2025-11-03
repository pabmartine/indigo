package com.martinia.indigo.mail.infrastructure.api.controllers;

import com.martinia.indigo.mail.domain.ports.usecases.SendMailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
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
public class SendMailController {

	@Resource
	private SendMailUseCase useCase;

	@Operation(summary = "Send mail",
			description = "Sends an email with a specified file to a given address.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Email sent successfully"),
					@ApiResponse(responseCode = "400", description = "Invalid request parameters or email sending failed")
			})
	@GetMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> send(
			@Parameter(description = "The path to the file to be sent", example = "/path/to/document.pdf") @RequestParam final String path,
			@Parameter(description = "The recipient's email address", example = "recipient@example.com") @RequestParam final String address) throws Exception {

		final String error = useCase.mail(path, address);

		if (StringUtils.isBlank(error)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		else {
			throw new Exception(error);
		}
	}

}
